package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;

public interface NotificationTaskService {
    boolean greetUserOnStart(Update update);

    boolean saveMessage(Update update);

    boolean checkForNonStringInput(String input, long chatId);
}
