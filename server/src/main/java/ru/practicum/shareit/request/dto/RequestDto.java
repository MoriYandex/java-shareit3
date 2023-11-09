package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RequestDto {
    private Long id;
    //@NotBlank
    private String description;
    private Long requestorId;
    private LocalDateTime created;

    public RequestDto(@JsonProperty("id") Long id,
                      @JsonProperty("description") String description,
                      @JsonProperty("requestorId") Long requestorId,
                      @JsonProperty("created") LocalDateTime created) {
        this.id = id;
        this.description = description;
        this.requestorId = requestorId;
        this.created = created;
    }
}
