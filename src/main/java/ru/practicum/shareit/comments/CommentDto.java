package ru.practicum.shareit.comments;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;
}
