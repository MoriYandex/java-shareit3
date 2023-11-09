package ru.practicum.shareit.request.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.user.User;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestMapper {
    public RequestDto toInDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor().getId())
                .created(request.getCreated())
                .build();
    }

    public RequestDtoExtended toOutDto(Request request, List<ItemDto> items) {
        return RequestDtoExtended.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor().getId())
                .created(request.getCreated())
                .items(items)
                .build();
    }

    public Request fromDto(RequestDto requestDto, User requestor) {
        return Request.builder()
                .id(requestDto.getId())
                .description(requestDto.getDescription())
                .requestor(requestor)
                .created(requestDto.getCreated())
                .build();
    }

}
