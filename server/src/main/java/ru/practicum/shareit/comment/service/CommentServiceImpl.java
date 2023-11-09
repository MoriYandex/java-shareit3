package ru.practicum.shareit.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentMapper;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentDto add(CommentDto commentDto, Long itemId, Long userId) {
        log.info("Создание отзыва пользователем {} для вещи {}", userId, itemId);
        User author = userRepository.findById(userId).orElse(null);
        if (author == null) {
            log.error("Не найден создатель отзыва по идентификатору {}!", userId);
            throw new NotFoundException(String.format("Не найден создатель отзыва по идентификатору %d!", userId));
        }
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            log.error("Не найдена вещь {} для отзыва!", itemId);
            throw new NotFoundException(String.format("Не найдена вещь %d для отзыва!", itemId));
        }
        List<Booking> bookingList = bookingRepository.findAllByItemAndBookerAndStatusAndEndIsBefore(item, author, BookingStatus.APPROVED, LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        if (bookingList.isEmpty()) {
            log.error("Не найдено подтверждение использование вещи {} пользователем {}!", itemId, userId);
            throw new ValidationException(String.format("Не найдено подтверждение использование вещи %d пользователем %d!", itemId, userId));
        }
        commentDto.setCreated(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        Comment comment = commentMapper.fromDto(commentDto, item, author);
        return commentMapper.toDto(commentRepository.saveAndFlush(comment));
    }
}
