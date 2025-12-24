package ru.practicum.server;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.practicum.server.Exception.EmailAlreadyExistsException;
import ru.practicum.server.user.User;
import ru.practicum.server.user.UserServiceImp;
import ru.practicum.server.user.dto.UserDto;
import ru.practicum.server.user.repo.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserServiceImp.class})
class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImp userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto validUserDto;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        validUserDto = new UserDto();
        validUserDto.setName("Test User");
        validUserDto.setEmail("test@example.com");
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        UserDto createdUser = userService.createUser(validUserDto);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("Test User");
        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(1);
        assertThat(allUsers.get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void createUser_shouldThrowExceptionForDuplicateEmail() {
        userService.createUser(validUserDto);

        UserDto duplicateUserDto = new UserDto();
        duplicateUserDto.setName("Another User");
        duplicateUserDto.setEmail("test@example.com");

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(duplicateUserDto));

        assertThat(exception.getMessage()).contains("Email test@example.com already exists");

        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(1);
    }

    @Test
    void createUser_shouldCreateMultipleUsersWithDifferentEmails() {
        UserDto user1 = new UserDto();
        user1.setName("User 1");
        user1.setEmail("user1@example.com");
        userService.createUser(user1);

        UserDto user2 = new UserDto();
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        userService.createUser(user2);

        UserDto user3 = new UserDto();
        user3.setName("User 3");
        user3.setEmail("user3@example.com");
        userService.createUser(user3);

        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(3);

        List<String> emails = allUsers.stream().map(User::getEmail).toList();

        assertThat(emails).containsExactlyInAnyOrder("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    void createUser_shouldTreatEmailAsCaseSensitive() {

        UserDto userLower = new UserDto();
        userLower.setName("Lower Case");
        userLower.setEmail("user@example.com");
        userService.createUser(userLower);

        UserDto userUpper = new UserDto();
        userUpper.setName("Upper Case");
        userUpper.setEmail("USER@EXAMPLE.COM");

        try {
            userService.createUser(userUpper);
            List<User> allUsers = userRepository.findAll();
            assertThat(allUsers).hasSize(2);
        } catch (EmailAlreadyExistsException e) {
            assertThat(e.getMessage()).contains("already exists");
        }
    }

    @Test
    void createUser_shouldMapAllFieldsCorrectly() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@company.com");

        UserDto createdUser = userService.createUser(userDto);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("John Doe");
        assertThat(createdUser.getEmail()).isEqualTo("john.doe@company.com");

        User dbUser = userRepository.findById(createdUser.getId()).orElseThrow();
        assertThat(dbUser.getName()).isEqualTo("John Doe");
        assertThat(dbUser.getEmail()).isEqualTo("john.doe@company.com");
    }

    @Test
    void createUser_shouldAllowReusingEmailAfterDeletion() {
        UserDto firstUser = new UserDto();
        firstUser.setName("First User");
        firstUser.setEmail("reusable@example.com");
        UserDto createdFirst = userService.createUser(firstUser);

        userService.deleteUserById(createdFirst.getId());

        UserDto secondUser = new UserDto();
        secondUser.setName("Second User");
        secondUser.setEmail("reusable@example.com");
        UserDto createdSecond = userService.createUser(secondUser);

        assertThat(createdSecond).isNotNull();
        assertThat(createdSecond.getEmail()).isEqualTo("reusable@example.com");

        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(1);
        assertThat(allUsers.get(0).getEmail()).isEqualTo("reusable@example.com");
    }

    @Test
    void createUser_shouldRollbackOnException() {
        UserDto user1 = new UserDto();
        user1.setName("User 1");
        user1.setEmail("user1@example.com");
        userService.createUser(user1);

        UserDto user2 = new UserDto();
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        userService.createUser(user2);
        UserDto duplicateUser = new UserDto();
        duplicateUser.setName("Duplicate User");
        duplicateUser.setEmail("user1@example.com");

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(duplicateUser));
        List<User> allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(2);
    }

    @Test
    void createUser_shouldUseExistsByEmailForValidation() {
        userRepository.deleteAll();

        User user = new User();
        user.setName("Test");
        user.setEmail("exists@test.com");
        userRepository.save(user);

        UserDto duplicateDto = new UserDto();
        duplicateDto.setName("Duplicate");
        duplicateDto.setEmail("exists@test.com");

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(duplicateDto));
        assertThat(exception.getMessage()).contains("already exists");
    }
}