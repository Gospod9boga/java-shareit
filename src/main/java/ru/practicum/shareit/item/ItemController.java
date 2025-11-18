package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader(USER_ID_HEADER) Long ownerId,
                              @Validated(ItemDto.Create.class) @RequestBody ItemDto itemDto) {
        log.info("Create item: {}", itemDto);
        return itemService.createItem(itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemsById(@PathVariable Long itemId) {
        log.info("Get item by: {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("Get all items for user: {}", ownerId);
        return itemService.getUserItems(ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestHeader(USER_ID_HEADER) Long ownerId,
                              @Validated(ItemDto.Update.class) @RequestBody ItemDto itemDto) {
        log.info("PATCH update for itemId: {}, ownerId: {}", itemId, ownerId);
        return itemService.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("Search items with text: {}", text);
        return itemService.searchItems(text);
    }
}
