package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoExtended;

import java.util.List;

public interface BookingService {
    BookingDtoExtended add(BookingDto bookingDto, Long userId);

    BookingDtoExtended get(Long bookingId, Long userId);

    List<BookingDtoExtended> getAllByUserId(Long userId, String state, Integer from, Integer size);

    BookingDtoExtended approve(Long Long, Boolean approved, Long userId);

    List<BookingDtoExtended> getAllForItems(Long userId, String state, Integer from, Integer size);
}
