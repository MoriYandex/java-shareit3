package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RequestRepository requestRepository;
    private final BookingRepository bookingRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto add(UserDto userDto) {
        log.info("Добавление пользователя");
        User user = userMapper.fromDto(userDto);
        return userMapper.toDto(userRepository.saveAndFlush(user));
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        log.info("Редактирование пользователя с идентификатором {}", userId);
        User toUpdate = getById(userId);
        if (!Strings.isBlank(userDto.getEmail())) {
            toUpdate.setEmail(userDto.getEmail());
        }
        if (!Strings.isBlank(userDto.getName())) {
            toUpdate.setName(userDto.getName());
        }
        return userMapper.toDto(userRepository.saveAndFlush(toUpdate));
    }

    @Override
    public UserDto get(Long userId) {
        log.info("Поиск пользователя с идентификатором {}", userId);
        return userMapper.toDto(getById(userId));
    }

    @Override
    public UserDto delete(Long userId) {
        log.info("Удаление пользователя с идентификатором {}", userId);
        User user = getById(userId);
        if (!bookingRepository.findAllByBooker(user, Pageable.unpaged()).isEmpty())
            throw new ValidationException(String.format("Пользователь с идентификатором %d имеет бронирования, удаление невозможно!", userId));
        if (!requestRepository.findAllByRequestorOrderByCreatedDesc(user).isEmpty())
            throw new ValidationException(String.format("Пользователь с идентификатором %d имеет запросы на создание вещи, удаление невозможно!", userId));
        if (!itemRepository.findAllByOwner(user, Pageable.unpaged()).isEmpty())
            throw new ValidationException(String.format("Пользователь с идентификатором %d имеет вещи, удаление невозможно!", userId));
        userRepository.deleteById(userId);
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        log.info("Получение всех пользователей");
        return userRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    private User getById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("Пользователь с идентификатором {} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с идентификатором %d не найден", userId));
        }
        return user;
    }
}
