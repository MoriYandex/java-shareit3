package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> add(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                      @RequestBody @Valid ItemDto itemDto) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        return itemClient.add(userId, itemDto);
    }

    @PatchMapping(path = "/{itemId}")
    public ResponseEntity<Object> update(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                         @Positive @PathVariable(name = "itemId") long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Updating item {}, itemId={}, userId={}", itemDto, itemId, userId);
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping(path = "/{itemId}")
    public ResponseEntity<Object> get(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                      @Positive @PathVariable(name = "itemId") long itemId) {
        log.info("Get item itemId={}, userId={}", itemId, userId);
        return itemClient.get(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                                  @PositiveOrZero @RequestParam(name = "from", required = false) Integer from,
                                                  @Positive @RequestParam(name = "size", required = false) Integer size) {
        log.info("Get items by owner userId={}, from={}, size={}", userId, from, size);
        return itemClient.getAllByOwnerId(userId, from, size);
    }

    @GetMapping(path = "/search")
    public ResponseEntity<Object> searchAvailableByText(@RequestParam(name = "text") String text,
                                                        @RequestParam(name = "from", required = false) Integer from,
                                                        @RequestParam(name = "size", required = false) Integer size) {
        log.info("Search items by text {}, from={}, size={}", text, from, size);
        return itemClient.searchAvailableByText(text, from, size);
    }

    @PostMapping(path = "/{itemId}/comment")
    public ResponseEntity<Object> addComment(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                             @Positive @PathVariable(name = "itemId") long itemId,
                                             @RequestBody @Valid CommentDto commentDto) {
        log.info("Creating comment {} to item itemId={}, userId={}", commentDto, itemId, userId);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}





