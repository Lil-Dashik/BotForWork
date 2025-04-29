package project.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import project.configuration.BotConfig;
import project.mapper.MessageMapper;
import project.dto.BotCommandDTO;
import project.service.kafka.GoToWorkCommandProducer;
import project.service.kafka.StartCommandProducer;
import project.service.kafka.StopCommandProducer;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommuteBot extends TelegramLongPollingBot {
    private final StartCommandProducer startCommandProducer;
    private final GoToWorkCommandProducer goToWorkCommandProducer;
    private final StopCommandProducer stopCommandProducer;
    private final BotConfig config;
    private final MessageMapper messageMapper;
    private final Map<Long, Boolean> userWaitingForData = new HashMap<>();
    private KafkaTemplate<String, Long> kafkaTemplate;


    @Autowired
    public CommuteBot(BotConfig config, StartCommandProducer startCommandProducer,
                      GoToWorkCommandProducer goToWorkCommandProducer,
                      StopCommandProducer stopCommandProducer,
                      MessageMapper messageMapper,
                      KafkaTemplate<String, Long> kafkaTemplate) {
        this.config = config;
        this.messageMapper = messageMapper;
        this.startCommandProducer = startCommandProducer;
        this.goToWorkCommandProducer = goToWorkCommandProducer;
        this.stopCommandProducer = stopCommandProducer;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();

        if (message != null && message.hasText()) {
            String messageText = message.getText();
            Long chatId = message.getChatId();

            if ("/start".equals(messageText)) {
                sendStartMessage(chatId);
                saveUserData(message);
            } else if ("/go_to_work".equals(messageText)) {
                sendGoToWorkMessage(chatId);
                userWaitingForData.put(chatId, true);
            } else if (userWaitingForData.containsKey(chatId) && userWaitingForData.get(chatId)) {
                saveUserDataWork(chatId, message);
                userWaitingForData.put(chatId, false);
            } else if ("/stop".equalsIgnoreCase(messageText)) {
                disableNotifications(chatId);
                sendMessage(chatId, "Уведомления отключены. Чтобы снова их включить, начните с команды /start.");
            } else {
                sendMessage(chatId, "Неверная команда. Вы отправили: " + messageText);
            }
        }
    }

    private void sendStartMessage(Long chatId) {
        String startMessage = "Привет! Я бот, который помогает прогнозировать время в пути до работы. \n" +
                "Выбери команду /go_to_work для того, чтобы начать расчёт времени.";

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(startMessage);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void saveUserData(Message message) {
        BotCommandDTO dto = new BotCommandDTO("start",
                message.getFrom().getId(),
                String.format("%s; %s", message.getFrom().getUserName(), message.getFrom().getFirstName())
        );
        startCommandProducer.send(dto);
    }

    private void saveUserDataWork(Long telegramId, Message message) {
        BotCommandDTO dto = new BotCommandDTO("go_to_work", telegramId, message.getText());
        goToWorkCommandProducer.send(dto);
    }

    private void disableNotifications(Long telegramUserId) {
        BotCommandDTO dto = new BotCommandDTO("stop", telegramUserId, null);
        stopCommandProducer.send(dto);
    }

    private void sendGoToWorkMessage(Long chatId) {
        String requestMessage = "Пожалуйста, отправьте <адрес дома>; <адрес работы>; и время ('HH:mm'), к которому нужно быть на работе. Например: \n" +
                "Москва, Красная площадь 1; Москва, Тверская улица 7; 09:00";

        SendMessage message = messageMapper.toSendMessage(chatId, requestMessage);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMarkNotified(Long telegramId) {
        kafkaTemplate.send("mark-notified", telegramId);
    }


    public void sendMessage(Long chatId, String text) {
        SendMessage message = messageMapper.toSendMessage(chatId, text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void initCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("start", "начать работу"),
                new BotCommand("go_to_work", "рассчитать время до работы"),
                new BotCommand("stop", "отключить уведомления")
        );

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }
}
