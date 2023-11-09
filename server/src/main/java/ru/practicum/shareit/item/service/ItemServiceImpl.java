package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentMapper;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoExtended;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto add(ItemDto itemDto, Long userId) {
        log.info("Добавление вещи");
        User owner = userRepository.findById(userId).orElse(null);
        if (owner == null) {
            log.error("Не найден владелец с идентификатором {} для добавления вещи!", userId);
            throw new NotFoundException(String.format("Не найден владелец с идентификатором %d для добавления вещи!", userId));
        }
        Request request = itemDto.getRequestId() != null
                ? requestRepository.findById(itemDto.getRequestId()).orElse(null)
                : null;
        Item item = itemMapper.fromDto(itemDto, owner, request);
        return itemMapper.toDto(itemRepository.saveAndFlush(item));
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long userId, Long itemId) {
        log.info("Редактирование вещи по идентификатору {}", itemId);
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            log.error("Вещь с идентификатором {} не найдена!", itemId);
            throw new NotFoundException(String.format("Вещь с идентификатором %d не найдена!", itemId));
        }
        if (!Objects.equals(userId, item.getOwner().getId())) {
            log.error("Попытка изменить владельца вещи {} c {} на {}!", itemId, item.getOwner().getId(), userId);
            throw new ForbiddenException(String.format("Попытка изменить владельца вещи %d c %d на %d!", itemId, item.getOwner().getId(), userId));
        }
        if (itemDto.getRequestId() != null && (item.getRequest() == null || !Objects.equals(itemDto.getRequestId(), item.getRequest().getId()))) {
            log.error("Попытка изменить запрос на создание вещи {} на {}!", itemId, itemDto.getRequestId());
            throw new ValidationException(String.format("Попытка изменить запрос на создание вещи %d на %d!", itemId, itemDto.getRequestId()));
        }
        if (!Strings.isBlank(itemDto.getName()))
            item.setName(itemDto.getName());
        if (!Strings.isBlank(itemDto.getDescription()))
            item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null)
            item.setAvailable(itemDto.getAvailable());
        return itemMapper.toDto(itemRepository.saveAndFlush(item));
    }

    @Override
    public ItemDtoExtended get(Long itemId, Long userId) {
        log.info("Поиск вещи по идентификатору {}", itemId);
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            log.error("Вещь с идентификатором {} не найдена!", itemId);
            throw new NotFoundException(String.format("Вещь с идентификатором %d не найдена!", itemId));
        }
        List<CommentDto> comments = commentRepository.getAllByItemOrderByCreatedDesc(item).stream().map(commentMapper::toDto).collect(Collectors.toList());
        if (!Objects.equals(item.getOwner().getId(), userId))
            return itemMapper.toDtoExtended(item, comments);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        Booking nextBooking = bookingRepository.findFirstByItemAndStatusAndStartIsAfterOrderByStart(item, BookingStatus.APPROVED, now);
        Booking lastBooking = bookingRepository.findFirstByItemAndStatusAndStartIsBeforeOrderByEndDesc(item, BookingStatus.APPROVED, now);
        return itemMapper.toDtoExtended(item,
                nextBooking != null ? bookingMapper.toInDto(nextBooking) : null,
                lastBooking != null ? bookingMapper.toInDto(lastBooking) : null,
                comments);
    }

    @Override
    public List<ItemDtoExtended> getAllByUserExtended(Long userId, Integer from, Integer size) {
        log.info("Поиск вещей по пользователю с идентификатором {} - полное описание", userId);
        User owner = userRepository.findById(userId).orElse(null);
        if (owner == null) {
            log.error("Не найден пользователь {} для поиска вещей!", userId);
            throw new NotFoundException(String.format("Не найден пользователь %d для поиска вещей!", userId));
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
                ? PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id"))
                : PageRequest.of(from / size, size, Sort.by("id")));
        List<Item> items = itemRepository.findAllByOwner(owner, pageable).toList();
        List<Booking> bookingList = bookingRepository.findAllByItemInOrderByStartDesc(items);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        List<Comment> commentList = commentRepository.getAllByOwner(owner);
        return items.stream().map(item -> {
            Booking nextBooking = bookingList.stream()
                    .filter(x -> Objects.equals(x.getItem().getId(), item.getId()) && x.getStart().isAfter(now) && x.getStatus() == BookingStatus.APPROVED).min(Comparator.comparing(Booking::getStart)).orElse(null);
            Booking lastBooking = bookingList.stream()
                    .filter(x -> Objects.equals(x.getItem().getId(), item.getId()) && x.getStart().isBefore(now) && x.getStatus() == BookingStatus.APPROVED).max(Comparator.comparing(Booking::getEnd)).orElse(null);
            List<CommentDto> comments = commentList.stream()
                    .filter(x -> Objects.equals(x.getItem().getId(), item.getId()))
                    .sorted(Comparator.comparing(Comment::getCreated).reversed())
                    .map(commentMapper::toDto)
                    .collect(Collectors.toList());
            return itemMapper.toDtoExtended(item,
                    nextBooking != null ? bookingMapper.toInDto(nextBooking) : null,
                    lastBooking != null ? bookingMapper.toInDto(lastBooking) : null,
                    comments);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getAvailableByText(String text, Integer from, Integer size) {
        log.info("Поиск вещей по описанию с текстом {}", text);
        if (Strings.isBlank(text))
            return new ArrayList<>();
        if (from != null && from < 0) {
            log.error("Значение номера первого элемента должно быть неотрицательно! Текущее значение номера {}", from);
            throw new ValidationException(String.format("\"Значение номера первого элемента должно быть неотрицательно! Текущее значение номера  %d", from));
        }
        if (size != null && size <= 0) {
            log.error("Значение размера страницы должны быть положительно! Текущее значение размера {}", size);
            throw new ValidationException(String.format("Значение размера страницы должны быть положительно! Текущее значение размера %d", size));
        }
        Pageable pageable = (from == null || size == null
                ? PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id"))
                : PageRequest.of(from / size, size, Sort.by("id")));
        return itemRepository.searchByText(true, text, pageable)
                .stream().map(itemMapper::toDto).collect(Collectors.toList());
    }
}
