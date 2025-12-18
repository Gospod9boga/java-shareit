package ru.practicum.shareit.comments;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
public class CommentMapper {
    public static CommentResponseDto toDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getAuthor().getName());
        if (comment.getCreatedDate() != null) {
            dto.setCreated(comment.getCreatedDate());
        }
        return dto;
    }

    public static Comment toEntity(CommentDto dto, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        try {
            Method setCreatedDate = Comment.class.getMethod("setCreatedDate", LocalDateTime.class);
            setCreatedDate.invoke(comment, LocalDateTime.now());
        } catch (Exception e) {
            log.warn("Could not set createdDate on Comment entity");
        }
        return comment;
    }
}
