package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

//import javax.validation.constraints.NotBlank;
//import javax.validation.constraints.NotNull;

@Data
@Builder
public class ItemDto {
    private Long id;
    //@NotBlank(message = "Наименование вещи не может быть пустым!")
    private String name;
    //@NotBlank(message = "Описание вещи не может быть пустым!")
    private String description;
    //@NotNull(message = "Состояние доступности вещи не может быть пустым!")
    private Boolean available;
    private Long ownerId;
    private Long requestId;

    public ItemDto(@JsonProperty("id") Long id,
                   @JsonProperty("name") String name,
                   @JsonProperty("description") String description,
                   @JsonProperty("available") Boolean available,
                   @JsonProperty("ownerId") Long ownerId,
                   @JsonProperty("requestId") Long requestId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.ownerId = ownerId;
        this.requestId = requestId;
    }
}
