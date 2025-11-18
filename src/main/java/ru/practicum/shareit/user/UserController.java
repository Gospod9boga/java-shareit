package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;


@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Create user :{} ", userDto);
        return userService.createUser(userDto);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Get user by id:{}", id);
        UserDto userDto = userService.getUserById(id);

        if (userDto == null) {
            throw new EntityNotFoundException("User with id = " + id + "not found");
        }
        return userDto;
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id,
                              @RequestBody UserDto userDto) {
        log.info("Update user:{}", userDto);
        return userService.updateUser(id, userDto);

    }

    @GetMapping
    public List<UserDto> getAllUser() {
        log.info("Get all user");
        List<UserDto> usersDto = userService.getAllUsers();
        return usersDto;
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable Long id) {
        log.info("Delete user with id{}", id);
        userService.deleteUserById(id);
    }
}
