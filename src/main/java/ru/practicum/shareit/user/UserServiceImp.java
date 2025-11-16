package ru.practicum.shareit.user;

import lombok.Data;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Service
public class UserServiceImp implements UserService {

    private Map<Long, User> users = new HashMap<>();
    private Long currentId = 0L;

    @Override
    public User createUser(User user) {
        boolean emailExists = users.values().stream()
                .anyMatch(existingUser -> existingUser.getEmail().equals(user.getEmail()));
        if (emailExists) {
            throw new ValidationException("Email" + user.getEmail() + "already busy");
        }
        users.put(currentId, user);
        user.setId(currentId);
        currentId++;
        return user;
    }

    @Override
    public User updateUser(Long userId, User user) {
        if (!users.containsKey(userId)) {
            throw new ValidationException("User with id " + userId + " not found");
        }

        User existingUser = users.get(userId);


        boolean emailTakenByOther = users.values().stream()
                .anyMatch(existing -> existing.getEmail().equals(user.getEmail()) &&
                        !existing.getId().equals(userId));
        if (emailTakenByOther) {
            throw new ValidationException("Email " + user.getEmail() + " already busy");
        }


        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }

        return existingUser;
    }

    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            throw new ValidationException("user with id" + userId + "not found ");
        }
        User user = users.get(userId);
        return user;
    }

    @Override
    public List<User> getAllUsers() {

        List<User> allUsers = new ArrayList<>(users.values());
        return allUsers;
    }

    @Override
    public void deleteUserById(Long userId) {
        users.remove(userId);

    }
}
