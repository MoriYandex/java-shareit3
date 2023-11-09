package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping()
    public ResponseEntity<Object> add(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                      @RequestBody @Valid RequestDto requestDto) {
        log.info("Creating request {}, userId={}", requestDto, userId);
        return requestClient.add(userId, requestDto);
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<Object> getById(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                          @Positive @PathVariable(name = "requestId") long requestId) {
        log.info("Get request requestId={}, userId={}", requestId, userId);
        return requestClient.getById(userId, requestId);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllByUserId(@Positive @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Get requests by userId={}", userId);
        return requestClient.getAllByUserId(userId);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<Object> getAll(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                         @PositiveOrZero @RequestParam(name = "from", required = false) Integer from,
                                         @Positive @RequestParam(name = "size", required = false) Integer size) {
        log.info("Get all requests, from={}, size={}", from, size);
        return requestClient.getAll(userId, from, size);
    }
}
