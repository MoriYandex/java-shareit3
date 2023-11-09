package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@GetMapping
	public ResponseEntity<Object> getAll(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
										 @RequestParam(name = "state", defaultValue = "all") String stateParam,
										 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
										 @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getAll(userId, state, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> add(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
									  @RequestBody @Valid BookItemRequestDto requestDto) {
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.add(userId, requestDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> get(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
									  @Positive @PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.get(userId, bookingId);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllByOwnerId(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
												  @RequestParam(name = "state", defaultValue = "all") String stateParam,
												  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
												  @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {} and owner by userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getAllByOwnerId(userId, state, from, size);
	}

	@PatchMapping(path = "/{bookingId}")
	public ResponseEntity<Object> approve(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
										  @Positive @PathVariable Long bookingId,
										  @NotNull Boolean approved) {
		log.info("Approve booking with id={} to status={}", bookingId, approved);
		return bookingClient.approve(userId, bookingId, approved);
	}
}
