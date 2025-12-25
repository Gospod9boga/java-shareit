package ru.practicum.server.comments;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.server.item.model.Item;
import ru.practicum.server.user.User;

@Slf4j
public class CommentMapper {
    public static CommentResponseDto toDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreatedDate());
        return dto;
    }

    public static Comment toEntity(CommentDto dto, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        return comment;
    }
}
