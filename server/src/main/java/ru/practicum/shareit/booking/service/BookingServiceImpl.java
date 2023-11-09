package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoExtended;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingStatusDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnsupportedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.dto.BookingStatusDto.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    private final Map<BookingStatusDto, Function<BookingSearchData, List<BookingDtoExtended>>> userHandlerMap = Map.of(
            ALL, this::handleAllByUser,
            CURRENT, this::handleCurrentByUser,
            PAST, this::handlePastByUser,
            FUTURE, this::handleFutureByUser,
            WAITING, this::handleWaitingByUser,
            REJECTED, this::handleRejectedByUser);
    private final Map<BookingStatusDto, Function<BookingSearchData, List<BookingDtoExtended>>> itemHandlerMap = Map.of(
            ALL, this::handleAllByItem,
            CURRENT, this::handleCurrentByItem,
            PAST, this::handlePastByItem,
            FUTURE, this::handleFutureByItem,
            WAITING, this::handleWaitingByItem,
            REJECTED, this::handleRejectedByItem);

    @Override
    public BookingDtoExtended add(BookingDto bookingDto, Long userId) {
        log.info("Создание бронирования для пользователя {} на вещь {}", userId, bookingDto.getItemId());
        User booker = userRepository.findById(userId).orElse(null);
        if (booker == null) {
            log.error("Не найден пользователь {} для бронирования!", userId);
            throw new NotFoundException(String.format("Не найден пользователь %d для бронирования!", userId));
        }
        Item item = itemRepository.findById(bookingDto.getItemId()).orElse(null);
        if (item == null) {
            log.error("Не найдена вещь с идентификатором {} для бронирования!", bookingDto.getItemId());
            throw new NotFoundException(String.format("Не найдена вещь с идентификатором %d для бронирования!", bookingDto.getItemId()));
        }
        if (!item.getAvailable()) {
            log.error("Вещь с идентификатором {} недоступна к бронированию!", item.getId());
            throw new ValidationException(String.format("Вещь с идентификатором %d недоступна к бронированию!", item.getId()));
        }
        if (userId.equals(item.getOwner().getId())) {
            log.error("Попытка пользователя {} забронировать собственную вещь {}", userId, item.getId());
            throw new NotFoundException(String.format("Попытка пользователя %d забронировать собственную вещь %d", userId, item.getId()));
        }
        bookingDto.setBookerId(userId);
        validateTime(bookingDto);
        Booking booking = bookingMapper.fromDto(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);
        Booking result = bookingRepository.saveAndFlush(booking);
        return bookingMapper.toOutDto(result, itemMapper.toDto(result.getItem()), userMapper.toDto(result.getBooker()));
    }

    @Override
    public BookingDtoExtended get(Long bookingId, Long userId) {
        log.info("Получение информации о бронировании {} пользователем {}", bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            log.error("Не найдено бронирование по идентификатору {}", bookingId);
            throw new NotFoundException(String.format("Не найдено бронирование по идентификатору %d", bookingId));
        }
        if (!Objects.equals(booking.getBooker().getId(), userId) && !Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            log.error("Вывод данных запрещён: пользователь с идентификатором {} не является автором бронирования либо владельцем вещи!", userId);
            throw new NotFoundException(String.format("Вывод данных запрещён: пользователь с идентификатором %d не является автором бронирования либо владельцем вещи!", userId));
        }
        return bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()));
    }

    @Override
    public List<BookingDtoExtended> getAllByUserId(Long userId, String state, Integer from, Integer size) {
        log.info("Получение бронирований по идентификатору пользователя {} и статусу {}", userId, state);
        BookingStatusDto bookingStatusDto = getState(state);
        User booker = userRepository.findById(userId).orElse(null);
        if (booker == null) {
            log.error("Не найден пользователь {} для бронирования!", userId);
            throw new NotFoundException(String.format("Не найден пользователь %d для бронирования!", userId));
        }
        if (from != null && from < 0) {
            log.error("Значение номера первого элемента должно быть неотрицательно! Текущее значение номера {}", from);
            throw new ValidationException(String.format("\"Значение номера первого элемента должно быть неотрицательно! Текущее значение номера  %d", from));
        }
        if (size != null && size <= 0) {
            log.error("Значение размера страницы должны быть положительно! Текущее значение размера {}", size);
            throw new ValidationException(String.format("Значение размера страницы должны быть положительно! Текущее значение размера %d", size));
        }
        Pageable pageable = (from == null || size == null
                ? PageRequest.of(0, Integer.MAX_VALUE, Sort.by("start").descending())
                : PageRequest.of(from / size, size, Sort.by("start").descending()));
        BookingSearchData data = new BookingSearchData(booker, pageable);
        return userHandlerMap.get(bookingStatusDto).apply(data);
    }

    private BookingStatusDto getState(String state) {
        try {
            return BookingStatusDto.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(String.format("Unknown state: %s", state));
        }
    }

    @Override
    public BookingDtoExtended approve(Long bookingId, Boolean approved, Long userId) {
        log.info("Подтверждение бронирования по идентификатору {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            log.error("Не найдено бронирование по идентификатору {}", bookingId);
            throw new NotFoundException(String.format("Не найдено бронирование по идентификатору %d", bookingId));
        }
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            log.error("Подтверждение запрещено: пользователь с идентификатором {} не является владельцем вещи с идентификатором {}", userId, booking.getItem().getId());
            throw new NotFoundException(String.format("Подтверждение запрещено: пользователь с идентификатором %d не является владельцем вещи с идентификатором %d", userId, booking.getItem().getId()));
        }
        if (booking.getStatus() == BookingStatus.WAITING)
            booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        else {
            log.error("Попытка изменить статус бронирования {}, не находящегося в ожидании!", bookingId);
            throw new ValidationException(String.format("Попытка изменить статус бронирования %d, не находящегося в ожидании!", bookingId));
        }
        Booking result = bookingRepository.saveAndFlush(booking);
        return bookingMapper.toOutDto(result, itemMapper.toDto(result.getItem()), userMapper.toDto(result.getBooker()));
    }

    @Override
    public List<BookingDtoExtended> getAllForItems(Long userId, String state, Integer from, Integer size) {
        log.info("Получение бронирований по вещам пользователя {} и статусу {}", userId, state);
        BookingStatusDto bookingStatusDto = getState(state);
        User owner = userRepository.findById(userId).orElse(null);
        if (owner == null) {
            log.error("Не найден пользователь {} для поиска бронирований по вещам!", userId);
            throw new NotFoundException(String.format("Не найден пользователь %d для поиска бронирований по вещам!", userId));
        }
        if (from != null && from < 0) {
            log.error("Значение номера первого элемента должно быть неотрицательно! Текущее значение номера {}", from);
            throw new ValidationException(String.format("\"Значение номера первого элемента должно быть неотрицательно! Текущее значение номера  %d", from));
        }
        if (size != null && size <= 0) {
            log.error("Значение размера страницы должны быть положительно! Текущее значение размера {}", size);
            throw new ValidationException(String.format("Значение размера страницы должны быть положительно! Текущее значение размера %d", size));
        }
        Pageable pageable = (from == null || size == null
                ? PageRequest.of(0, Integer.MAX_VALUE, Sort.by("start").descending())
                : PageRequest.of(from / size, size, Sort.by("start").descending()));
        BookingSearchData data = new BookingSearchData(owner, pageable);
        return itemHandlerMap.get(bookingStatusDto).apply(data);
    }

    private void validateTime(BookingDto bookingDto) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        if (start.isBefore(now) || end.isBefore(now) || !start.isBefore(end)) {
            log.error("Неверно заданы параметры времени для бронирования вещи {} пользователем {}", bookingDto.getItemId(), bookingDto.getBookerId());
            throw new ValidationException(String.format("Неверно заданы параметры времени для бронирования вещи %d пользователем %d", bookingDto.getItemId(), bookingDto.getBookerId()));
        }
    }

    private List<BookingDtoExtended> handleAllByUser(BookingSearchData data) {
        return bookingRepository.findAllByBooker(data.user, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handleCurrentByUser(BookingSearchData data) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        return bookingRepository.findAllByBookerAndStartIsBeforeAndEndIsAfter(data.user, now, now, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handlePastByUser(BookingSearchData data) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        return bookingRepository.findAllByBookerAndEndIsBefore(data.user, now, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handleFutureByUser(BookingSearchData data) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        return bookingRepository.findAllByBookerAndStartIsAfter(data.user, now, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handleWaitingByUser(BookingSearchData data) {
        return bookingRepository.findAllByBookerAndStatus(data.user, BookingStatus.WAITING, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handleRejectedByUser(BookingSearchData data) {
        return bookingRepository.findAllByBookerAndStatus(data.user, BookingStatus.REJECTED, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handleAllByItem(BookingSearchData data) {
        return bookingRepository.findAllByItemOwner(data.user, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handleCurrentByItem(BookingSearchData data) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        return bookingRepository.findAllByItemOwnerCurrent(data.user, now, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handlePastByItem(BookingSearchData data) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        return bookingRepository.findAllByItemOwnerPast(data.user, now, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handleFutureByItem(BookingSearchData data) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        return bookingRepository.findAllByItemOwnerFuture(data.user, now, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handleWaitingByItem(BookingSearchData data) {
        return bookingRepository.findAllByItemOwnerByStatus(data.user, BookingStatus.WAITING, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    private List<BookingDtoExtended> handleRejectedByItem(BookingSearchData data) {
        return bookingRepository.findAllByItemOwnerByStatus(data.user, BookingStatus.REJECTED, data.pageable).stream().map(booking ->
                bookingMapper.toOutDto(booking, itemMapper.toDto(booking.getItem()), userMapper.toDto(booking.getBooker()))).collect(Collectors.toList());
    }

    @AllArgsConstructor
    static class BookingSearchData {
        User user;
        Pageable pageable;
    }
}
