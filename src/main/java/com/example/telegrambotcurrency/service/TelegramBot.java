package com.example.telegrambotcurrency.service;

import com.example.telegrambotcurrency.config.BotConfig;
import com.example.telegrambotcurrency.integration.ConverterApi;
import com.example.telegrambotcurrency.integration.payload.request.ConvertRequest;
import com.example.telegrambotcurrency.model.MessagesForDb;
import com.example.telegrambotcurrency.model.User;
import com.example.telegrambotcurrency.repository.MessageRepository;
import com.example.telegrambotcurrency.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private final ConverterApi converterApi;

    final BotConfig config;

    static final String HELP_DEFAULT_TEXT = "Этот бот предназначен для конвертации валют в режиме реального времени.\n\n" +
            "Вы можете использовать команды из главного меню слева или введя команды:\n\n" +
            "Введите /start, чтобы увидеть приветственное сообщение.\n" +
            "Введите /mydata, чтобы просмотреть данные, хранящиеся о пользователе.\n" +
            "Введите /help, чтобы снова увидеть это сообщение.\n" +
            "Введите /clear, чтобы удалить историю сообщения с ботом.";

    public TelegramBot(ConverterApi converterApi, BotConfig config) {
        this.converterApi = converterApi;
        this.config = config;

        //Список комманд которые есть в боте
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "Запускает бота и показывает приветственное"));
        listofCommands.add(new BotCommand("/convert", "Конвертация валюты"));
        listofCommands.add(new BotCommand("/mydata", "Показывает информацию о пользователе"));
        listofCommands.add(new BotCommand("/help", "Информация как пользоваться ботом"));
        listofCommands.add(new BotCommand("/clear", "Удаляет историю бота"));

        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException ignore) {

        }
    }

    //С конфига берет название бота и позволяет настроить бот
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    //С конфига берет токен и позволяет настривать бот
    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            saveMessage(update.getMessage());
            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_DEFAULT_TEXT);
                    break;
                case "/convert":
                    sendMessage(chatId, "Введите значения в формате 'USD KZT 100' или " +
                            "KZT USD 450.");
                    break;
                case "/mydata":
                    myData(update);
                    break;
                case "/clear":
                    clear(update);
                    break;
                default:
                    try {
                        currencyConverter(chatId, update.getMessage());
                    }catch (NullPointerException e){
                        sendMessage(chatId, "Не правильно введена валюта, \n" +
                                "пожалуйста, введите еще раз.");

                    }
            }
        }
    }

    //Метод - registerUser позволяет сохранить пользователей в базу данных, которые пользовались ботом
    //сохраняет по времени первого пользования пользователем и по chatId();
    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirsName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
        }
    }

    //Отправляется сообщение при команде /start
    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет, " + name + ", рад знакомству!";
        sendMessage(chatId, answer);
    }

    //Отправленные сообщения боту сохраняются в базе данных PostgreSQL
    private void saveMessage(Message message){
        MessagesForDb messagesForDb = new MessagesForDb();
        messagesForDb.setIdChat(message.getChatId());
        messagesForDb.setText(message.getText());
        messagesForDb.setMessageTime(LocalDateTime.now());
        messageRepository.save(messagesForDb);

    }

    //Отправляет/выводит сообщение
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException ignored) {
        }
    }

    //Главный метод в этом классе, который конвертирует валюту
    private void currencyConverter(long chatId, Message message){
        String[] parts = message.getText().split(" ");
        if (parts.length == 3) {
            //Тут сохраняются в список отправленные пользователем строки по порядку возрастания.
            String fromCurrency = parts[0];
            String toCurrency = parts[1];
            double amount = Double.parseDouble(parts[2]);

            try {
                // Выполняется конвертацию валюты
                Double convertedAmount = converterApi.convert(new ConvertRequest(fromCurrency , toCurrency , amount)).getResult();
                // Формируется сообщение с результатом
                String resultMessage = "Результат конвертации: " + amount + "" + fromCurrency + " = " + convertedAmount + "" + toCurrency + ";";
                // Отправляется контрольное сообщение пользователю
                SendMessage sendMessage = new SendMessage(String.valueOf(chatId), resultMessage);
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    //Медот myData выводит информацию о пользователе
    private void myData(Update update){
        long chatId = update.getMessage().getFrom().getId();

        User user = userRepository.findById(chatId).orElse(null);
        if(user!=null){
            String result = "ID: " + user.getChatId() + "\n" + "Username: " + user.getUsername() + "\n"
                    + "Имя: " + user.getFirsName() + "\n" + "Фамилия: " + user.getLastName();
            sendMessage(chatId, result);
        }
    }

    //Команда /clear позволяет удалить историю сообщении с ботом
    private void clear(Update update){
       long chatId = update.getMessage().getChatId();
       int messageId = update.getMessage().getMessageId();
      while (update.hasMessage() && update.getMessage().hasText()) {
            for(int i = messageId - 1; i > 0; i--){
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), i);
                try {
                    execute(deleteMessage);
                }catch (TelegramApiException e){
                    e.printStackTrace();
                }
            }
       }
       String responseText = "История диалога очищена.";
       SendMessage response = new SendMessage(String.valueOf(chatId), responseText);
       try {
           execute(response);
       }catch (TelegramApiException e){
           e.printStackTrace();
       }
    }

}