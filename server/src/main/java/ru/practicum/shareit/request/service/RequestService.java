package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestDtoExtended;

import java.util.List;

public interface RequestService {
    RequestDto add(RequestDto requestDto, Long userId);

    RequestDtoExtended getById(Long requestId, Long userId);

    List<RequestDtoExtended> getAllByUserId(Long userId);

    List<RequestDtoExtended> getAll(Long userId, Integer from, Integer size);
}
