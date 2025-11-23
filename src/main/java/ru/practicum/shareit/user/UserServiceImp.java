package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.EntityNotFoundException;
import ru.practicum.shareit.Exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userStorage.existsByEmail(userDto.getEmail())) {
            throw new ValidationException("Email " + userDto.getEmail() + " already busy");
        }

        User user = UserMapper.toEntity(userDto);
        User savedUser = userStorage.save(user);
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userStorage.findById(userId)
                .orElseThrow(() -> new ValidationException("User with id " + userId + " not found"));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            if (userStorage.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
                throw new ValidationException("Email " + userDto.getEmail() + " already busy");
            }
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userStorage.save(existingUser);
        return UserMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
               User user = userStorage.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userStorage.findAll();
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserById(Long userId) {
        if (!userStorage.findById(userId).isPresent()) {
            throw new ValidationException("User with id " + userId + " not found");
        }
        userStorage.deleteById(userId);
    }
}
