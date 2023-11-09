package ru.practicum.shareit.booking;

public enum BookingStatus {
    WAITING("Новое бронирование, ожидает одобрения"),
    APPROVED("Бронирование подтверждено владельцем"),
    REJECTED("Бронирование отклонено владельцем"),
    CANCELED("Бронирование отменено создателем");
    private final String value;

    BookingStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public BookingStatus getStatus(String value) {
        try {
            return BookingStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
