package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto add(UserDto userDto);

    UserDto update(UserDto userDto, Long userId);

    UserDto get(Long userId);

    UserDto delete(Long userId);

    List<UserDto> getAll();
}
