package ru.practicum.gateway.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                             @Valid @RequestBody CommentRequestDto commentDto,
                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Add comment for itemId: {}, userId: {}", itemId, userId);
        return itemClient.addComment(userId, itemId, commentDto);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                             @Valid @RequestBody ItemRequestDto itemDto) {
        log.info("Create item: {}", itemDto);
        return itemClient.createItem(ownerId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemsById(@PathVariable Long itemId,
                                               @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Get item by id: {}, userId: {}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                               @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                               @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Get all items for user: {}", ownerId);
        return itemClient.getUserItems(ownerId, from, size);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable Long itemId,
                                             @RequestHeader(USER_ID_HEADER) Long ownerId,
                                             @Valid @RequestBody ItemRequestDto itemDto) {
        log.info("PATCH update for itemId: {}, ownerId: {}", itemId, ownerId);
        return itemClient.updateItem(ownerId, itemId, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @RequestParam String text,
                                              @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                              @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Search items with text: {}", text);
        return itemClient.searchItems(userId, text, from, size);
    }
}
