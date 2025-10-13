package ru.practicum.events;

public enum EventStateAction {
    // Действия пользователя
    SEND_TO_REVIEW,
    CANCEL_REVIEW,

    // Действия админа
    PUBLISH_EVENT,
    REJECT_EVENT
}
