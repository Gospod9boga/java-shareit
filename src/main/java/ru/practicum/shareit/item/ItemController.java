package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemService;

import java.util.stream.Collectors;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @Validated(ItemDto.Create.class) @RequestBody ItemDto itemDto) {
        log.info("Create item: {}", itemDto);
        Item item = ItemMapper.toEntity(itemDto);
        Item saveItem = itemService.createItem(item, ownerId);
        return ItemMapper.toDto(saveItem);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemsById(@PathVariable Long itemId) {
        log.info("Get item by:{}", itemId);
        Item item = itemService.getItemById(itemId);
        return ItemMapper.toDto(item);
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Get all items for user: {}", ownerId);
        List<Item> userItems = itemService.getUserItems(ownerId);
        return userItems.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @Validated(ItemDto.Update.class) @RequestBody ItemDto itemDto) {  // ← Используем группу Update!

        log.info("PATCH update for itemId: {}, ownerId: {}, updates: {}", itemId, ownerId, itemDto);

        Item itemUpdates = ItemMapper.toEntity(itemDto);
        Item updatedItem = itemService.updateItem(itemId, itemUpdates, ownerId);
        return ItemMapper.toDto(updatedItem);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("Search items with text: {}", text);
        List<Item> foundItems = itemService.searchItems(text);
        return foundItems.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}
