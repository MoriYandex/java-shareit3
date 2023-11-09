package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

//import javax.validation.constraints.Email;
//import javax.validation.constraints.NotBlank;

@Data
@Builder
public class UserDto {
    private Long id;
    private String name;
    //@Email(message = "Email пользователя должен соответствовать формату адреса электронной почты!")
    //@NotBlank(message = "Email пользователя не должен быть пустым!")
    private String email;

    public UserDto(@JsonProperty("id") Long id, @JsonProperty("name") String name, @JsonProperty("email") String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
