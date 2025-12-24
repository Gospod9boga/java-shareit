package ru.practicum.server.item.model;

import ru.practicum.server.comments.CommentDto;
import ru.practicum.server.comments.CommentResponseDto;
import ru.practicum.server.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemDto getItemById(Long itemId, Long userId);

    List<ItemDto> getUserItems(Long ownerId);

    List<ItemDto> searchItems(String text);

    void deleteItem(Long itemId, Long ownerId);

    CommentResponseDto addComment(Long itemId, CommentDto commentDto, Long userId);

}
