package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoExtended;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping()
    public BookingDtoExtended add(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody BookingDto bookingDto) {
        return bookingService.add(bookingDto, userId);
    }

    @PatchMapping(path = "/{bookingId}")
    public BookingDtoExtended approve(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable(name = "bookingId") Long bookingId, @RequestParam(name = "approved") Boolean approved) {
        return bookingService.approve(bookingId, approved, userId);
    }

    @GetMapping(path = "/{bookingId}")
    public BookingDtoExtended get(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable(name = "bookingId") Long bookingId) {
        return bookingService.get(bookingId, userId);
    }

    @GetMapping()
    public List<BookingDtoExtended> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(name = "state", defaultValue = "ALL") String state,
                                           @RequestParam(name = "from", required = false) Integer from,
                                           @RequestParam(name = "size", required = false) Integer size) {
        return bookingService.getAllByUserId(userId, state, from, size);
    }

    @GetMapping(path = "/owner")
    public List<BookingDtoExtended> getAllForItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                   @RequestParam(name = "from", required = false) Integer from,
                                                   @RequestParam(name = "size", required = false) Integer size) {
        return bookingService.getAllForItems(userId, state, from, size);
    }
}
