package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoExtended;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping()
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto itemDto) {
        return itemService.add(itemDto, userId);
    }

    //На update автоматическая валидация работать не будет, так как можно передавать неполные данные!
    @PatchMapping(path = "/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable(name = "itemId") Long itemId, @RequestBody ItemDto itemDto) {
        return itemService.update(itemDto, userId, itemId);
    }

    @GetMapping(path = "/{itemId}")
    public ItemDtoExtended get(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable(name = "itemId") Long itemId) {
        return itemService.get(itemId, userId);
    }

    @GetMapping()
    public List<ItemDtoExtended> getAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(name = "from", required = false) Integer from,
                                                @RequestParam(name = "size", required = false) Integer size) {
        return itemService.getAllByUserExtended(userId, from, size);
    }

    @GetMapping(path = "/search")
    public List<ItemDto> getAvailableByText(@RequestParam(name = "text") String text,
                                            @RequestParam(name = "from", required = false) Integer from,
                                            @RequestParam(name = "size", required = false) Integer size) {
        return itemService.getAvailableByText(text, from, size);
    }

    @PostMapping(path = "/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable(name = "itemId") Long itemId, @RequestBody CommentDto commentDto) {
        return commentService.add(commentDto, itemId, userId);
    }
}
