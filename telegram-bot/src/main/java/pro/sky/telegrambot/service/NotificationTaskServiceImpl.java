package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskServiceImpl implements NotificationTaskService {
    private final Logger logger = LoggerFactory.getLogger(NotificationTaskServiceImpl.class);
    private final Pattern pattern = Pattern
            .compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s([a-zA-zа-яА-я\\s\\,\\.[0-9]]+)*");
    private final String patternExample = "Example: 04.11.2023 08:00 Water the flowers";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private TelegramBot telegramBot;
    private NotificationTaskRepository repository;

    public NotificationTaskServiceImpl(TelegramBot telegramBot,
                                       NotificationTaskRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    /**
     * Method that greets user on "/start"
     *
     * @param update
     * @return true if user sent "/start" and false if not
     */
    @Override
    public boolean greetUserOnStart(Update update) {
        logWhenMethodInvoked("greetUserOnStart");
        long chatId = update.message().chat().id();
        String text = update.message().text();
        if (text.equals("/start")) {
            telegramBot.execute(new SendMessage(chatId, "Hi there! It's notification bot. " +
                    "I'll notify you when it's time to do a task.\n" +
                    "Type info about your task: date, time, text.\n" + patternExample));
            return true;
        }
        logger.info("Message is not '/start'");
        return false;
    }

    /**
     * Saves message from user to DB
     *
     * @param update
     * @return true if message matches the pattern and if it was successfully saved.
     * False if message doesn't match or if date is incorrect and hasn't been saved.
     * Also checks whether the date is correct(cannot be in the past).
     */
    @Override
    public boolean saveMessage(Update update) {
        logWhenMethodInvoked("saveMessage");
        long chatId = update.message().chat().id();
        String text = update.message().text();
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            try {
                LocalDateTime time = LocalDateTime
                        .parse(matcher.group(1), formatter);
                if (!checkIfDateIsCorrect(update, time)) {
                    return false;
                }
                repository.save(new NotificationTask(
                        chatId,
                        matcher.group(2),
                        time
                ));
                telegramBot.execute(new SendMessage(chatId, "Task has been saved. " +
                        "Rest assured you will get a notification message in the right time."));
                logger.info("Date is correct, task was saved to DB");
                return true;
            } catch (DateTimeParseException e) {
                telegramBot.execute(new SendMessage(chatId,
                        "Please look at the date and time, you have entered the incorrect value."));
                logger.info("Date is incorrect, corresponding message was sent to user");
            }
        } else {
            telegramBot.execute(
                    new SendMessage(chatId, "Something went wrong.\n" + patternExample)
            );
        }
        logger.info("Message is incorrect");
        return false;
    }

    /**
     * Sends notification and then deletes it from DB
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void checkTasks() {
        logger.info("Started searching tasks which need to be sent");
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        ArrayList<NotificationTask> tasks = new ArrayList<>(repository.findByNotifyTime(now));

        for (NotificationTask task : tasks) {
            telegramBot.execute(new SendMessage(task.getChatId(), task.getMessage()));
            repository.delete(task);
        }
    }

    @Override
    public boolean checkForNonStringInput(String input, long chatId) {
        logWhenMethodInvoked("checkForNonStringInput");
        if (input == null) {
            telegramBot.execute(new SendMessage(chatId, "I can only save text messages"));
            logger.info("User sent non text message, corresponding message was sent");
            return true;
        }
        return false;
    }

    private boolean checkIfDateIsCorrect(Update update, LocalDateTime time) {
        if (time.isBefore(LocalDateTime.now())) {
            telegramBot.execute(new SendMessage(update.message().chat().id(), "Date cannot be in the past"));
            logger.info("Date is in the past, corresponding message was sent to user");
            return false;
        }
        return true;
    }

    private void logWhenMethodInvoked(String methodName) {
        logger.info("Method {} was invoked", methodName);
    }
}
