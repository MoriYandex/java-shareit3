package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoExtended;

import java.util.List;

public interface ItemService {
    ItemDto add(ItemDto itemDto, Long userId);

    ItemDto update(ItemDto itemDto, Long userId, Long itemId);

    ItemDtoExtended get(Long itemId, Long userId);

    List<ItemDtoExtended> getAllByUserExtended(Long userId, Integer from, Integer size);

    List<ItemDto> getAvailableByText(String text, Integer from, Integer size);
}
