package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;


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
        User user = UserMapper.toEntity(userDto);
        User savedUser = userService.createUser(user);
        return UserMapper.toDTO(savedUser);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Get user by id:{}", id);
        User user = userService.getUserById(id);
        if (user == null) {
            throw new EntityNotFoundException("User with id = " + id + "not found");
        }
        return UserMapper.toDTO(user);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id,
                              @RequestBody UserDto userDto) {
        log.info("Update user:{}", userDto);

        User user = UserMapper.toEntity(userDto);
        User updateUser = userService.updateUser(id, user);
        return UserMapper.toDTO(updateUser);

    }

    @GetMapping
    public List<UserDto> getAllUser() {
        log.info("Get all user");
        List<User> users = userService.getAllUsers();
        List<UserDto> usersDto = users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
        return usersDto;
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable Long id) {
        log.info("Delete user with id{}", id);
        userService.deleteUserById(id);
    }
}
