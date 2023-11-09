package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
    private Long bookerId;
    private BookingStatus status;

    public BookingDto(@JsonProperty("id") Long id,
                      @JsonProperty("start") LocalDateTime start,
                      @JsonProperty("end") LocalDateTime end,
                      @JsonProperty("itemId") Long itemId,
                      @JsonProperty("bookerId") Long bookerId,
                      @JsonProperty("status") BookingStatus status) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.itemId = itemId;
        this.bookerId = bookerId;
        this.status = status;
    }
}
