package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private NotificationTaskService service;
    private TelegramBot telegramBot;

    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskService service) {
        this.telegramBot = telegramBot;
        this.service = service;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (service.checkForNonStringInput(update.message().text(), update.message().chat().id())) {
                return;
            }
            if (!service.greetUserOnStart(update)) {
                service.saveMessage(update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
