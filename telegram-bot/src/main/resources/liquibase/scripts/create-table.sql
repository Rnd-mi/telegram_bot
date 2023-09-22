--liquibase formatted sql

--changeset Rndmi:1
CREATE TABLE notification_task(
    id serial PRIMARY KEY,
    chat_id INT NOT NULL,
    message TEXT NOT NULL,
    notify_time TIMESTAMP NOT NULL
);

--changeset Rndmi:2
ALTER TABLE notification_task
ALTER COLUMN chat_id TYPE BIGINT;
