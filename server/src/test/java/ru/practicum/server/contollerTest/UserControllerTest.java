package ru.practicum.server.contollerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.server.Exception.EmailAlreadyExistsException;
import ru.practicum.server.Exception.EntityNotFoundException;
import ru.practicum.server.Exception.ValidationException;
import ru.practicum.server.user.UserController;
import ru.practicum.server.user.UserService;
import ru.practicum.server.user.dto.UserDto;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        when(userService.createUser(any(UserDto.class)))
                .thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void createUser_shouldReturnBadRequestWhenInvalidEmail() throws Exception {
        UserDto invalidUser = new UserDto();
        invalidUser.setName("Test");
        invalidUser.setEmail("invalid-email");

        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new ValidationException("Invalid email format"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email format"));
    }

    @Test
    void createUser_shouldReturnConflictForDuplicateEmail() throws Exception {
        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    void createUser_shouldReturnOkWhenEmptyName() throws Exception {
        UserDto userWithEmptyName = new UserDto();
        userWithEmptyName.setName("");
        userWithEmptyName.setEmail("test@example.com");

        when(userService.createUser(any(UserDto.class)))
                .thenReturn(userWithEmptyName);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userWithEmptyName)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void getUserById_shouldReturnNotFoundForInvalidId() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void getUserById_shouldReturnInternalServerErrorForInvalidPath() throws Exception {
        mockMvc.perform(get("/users/invalid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MethodArgumentTypeMismatchException")));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void updateUser_shouldReturnNotFoundForNonExistingUser() throws Exception {
        when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void updateUser_shouldReturnConflictForDuplicateEmail() throws Exception {
        when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already in use"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already in use"));
    }

    @Test
    void updateUser_shouldReturnInternalServerErrorForInvalidPath() throws Exception {
        mockMvc.perform(patch("/users/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MethodArgumentTypeMismatchException")));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        UserDto userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setName("User 2");
        userDto2.setEmail("user2@example.com");

        when(userService.getAllUsers())
                .thenReturn(List.of(userDto, userDto2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getAllUsers_shouldReturnEmptyList() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void removeUser_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUserById(anyLong());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void removeUser_shouldReturnNotFoundForNonExistingUser() throws Exception {
        doThrow(new EntityNotFoundException("User not found"))
                .when(userService).deleteUserById(anyLong());

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void removeUser_shouldReturnInternalServerErrorForInvalidPath() throws Exception {
        mockMvc.perform(delete("/users/invalid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MethodArgumentTypeMismatchException")));
    }

    @Test
    void createUser_shouldReturnOkWhenEmailNull() throws Exception {
        UserDto userWithoutEmail = new UserDto();
        userWithoutEmail.setName("Test");
        userWithoutEmail.setEmail(null);

        when(userService.createUser(any(UserDto.class)))
                .thenReturn(userWithoutEmail);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userWithoutEmail)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_shouldAllowPartialUpdate() throws Exception {
        UserDto partialUpdate = new UserDto();
        partialUpdate.setName("Updated Name Only");
        partialUpdate.setEmail(null);

        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setName("Updated Name Only");
        updatedUser.setEmail("test@example.com");

        when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name Only"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}