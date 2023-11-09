package ru.practicum.shareit.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.users.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping()
    public ResponseEntity<Object> add(@RequestBody @Valid UserDto userDto) {
        log.info("Creating user {}", userDto);
        return userClient.addUser(userDto);
    }

    @PatchMapping(path = "/{userId}")
    public ResponseEntity<Object> update(@Positive @PathVariable(name = "userId") long userId,
                                         @RequestBody UserDto userDto) {
        log.info("Updating user {}, userId={}", userDto, userId);
        return userClient.updateUser(userId, userDto);
    }

    @GetMapping()
    public ResponseEntity<Object> getAll() {
        log.info("Get all users");
        return userClient.getAll();
    }

    @GetMapping(path = "/{userId}")
    public ResponseEntity<Object> get(@Positive @PathVariable(name = "userId") long userID) {
        log.info("Get user userId={}", userID);
        return userClient.get(userID);
    }

    @DeleteMapping(path = "/{userId}")
    public ResponseEntity<Object> delete(@Positive @PathVariable(name = "userId") long userId) {
        log.info("Deleting user userId={}", userId);
        return userClient.delete(userId);
    }
}
