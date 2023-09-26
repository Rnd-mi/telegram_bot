package pro.sky.telegrambot.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notification_task")
public class NotificationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @Column(nullable = false)
    private String message;

    @Column(name = "notify_time", nullable = false)
    private LocalDateTime notifyTime;

    public NotificationTask() {
    }

    public NotificationTask(long chatId, String message, LocalDateTime notifyTime) {
        this.chatId = chatId;
        this.message = message;
        this.notifyTime = notifyTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(LocalDateTime notifyTime) {
        this.notifyTime = notifyTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificationTask)) {
            return false;
        }
        NotificationTask that = (NotificationTask) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
