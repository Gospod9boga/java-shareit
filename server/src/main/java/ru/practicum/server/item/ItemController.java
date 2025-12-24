package ru.practicum.server.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.comments.CommentDto;
import ru.practicum.server.comments.CommentResponseDto;
import ru.practicum.server.item.dto.ItemDto;
import ru.practicum.server.item.model.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
public class  ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@PathVariable Long itemId,
                                         @RequestBody CommentDto commentDto,
                                         @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Add comment for itemId: {}, userId: {}", itemId, userId);
        return itemService.addComment(itemId, commentDto, userId);
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader(USER_ID_HEADER) Long ownerId,
                              @RequestBody ItemDto itemDto) {
        log.info("Create item: {}", itemDto);
        return itemService.createItem(itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemsById(@PathVariable Long itemId,
                                @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Get item by id: {}, userId: {}", itemId, userId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("Get all items for user: {}", ownerId);
        return itemService.getUserItems(ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestHeader(USER_ID_HEADER) Long ownerId,
                              @RequestBody ItemDto itemDto) {
        log.info("PATCH update for itemId: {}, ownerId: {}", itemId, ownerId);
        return itemService.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("Search items with text: {}", text);
        return itemService.searchItems(text);
    }
}
