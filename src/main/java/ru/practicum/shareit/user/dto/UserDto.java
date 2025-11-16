package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Data
public class UserDto {
    private Long id;
    private String name;
    @Email
    @NotBlank
    private String email;

}
