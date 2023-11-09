package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestDtoExtended;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestService requestService;

    @PostMapping()
    public RequestDto add(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody RequestDto requestDto) {
        return requestService.add(requestDto, userId);
    }

    @GetMapping(path = "/{requestId}")
    public RequestDtoExtended getById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable(name = "requestId") Long requestId) {
        return requestService.getById(requestId, userId);
    }

    @GetMapping()
    public List<RequestDtoExtended> getAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getAllByUserId(userId);
    }

    @GetMapping(path = "/all")
    public List<RequestDtoExtended> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(name = "from", required = false) Integer from,
                                           @RequestParam(name = "size", required = false) Integer size) {
        return requestService.getAll(userId, from, size);
    }
}
