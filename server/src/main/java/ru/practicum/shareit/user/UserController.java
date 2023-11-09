package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping()
    public UserDto add(@RequestBody UserDto userDto) {
        return userService.add(userDto);
    }

    //На update автоматическая валидация работать не будет, так как можно передавать неполные данные!
    @PatchMapping(path = "/{userId}")
    public UserDto update(@PathVariable(name = "userId") Long userId, @RequestBody UserDto userDto) {
        return userService.update(userDto, userId);
    }

    @GetMapping()
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @GetMapping(path = "/{userId}")
    public UserDto get(@PathVariable(name = "userId") Long userID) {
        return userService.get(userID);
    }

    @DeleteMapping(path = "/{userId}")
    public UserDto delete(@PathVariable(name = "userId") Long userId) {
        return userService.delete(userId);
    }
}
