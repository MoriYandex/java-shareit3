package ru.practicum.shareit.item.dto;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.user.User;

import java.util.List;

@Component
public class ItemMapper {
    public ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner().getId())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public ItemDtoExtended toDtoExtended(Item item, List<CommentDto> comments) {
        return toDtoExtended(item, null, null, comments);
    }

    public ItemDtoExtended toDtoExtended(Item item, BookingDto nextBooking, BookingDto lastBooking, List<CommentDto> comments) {
        return ItemDtoExtended.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner().getId())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .nextBooking(nextBooking)
                .lastBooking(lastBooking)
                .comments(comments)
                .build();
    }

    public Item fromDto(ItemDto itemDto, User owner, @Nullable Request request) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }
}
