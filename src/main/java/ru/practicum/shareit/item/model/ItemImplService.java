package ru.practicum.shareit.item.model;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.AccessDeniedException;
import ru.practicum.shareit.Exception.EntityNotFoundException;
import ru.practicum.shareit.Exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemImplService implements ItemService {
    private final UserService userService;
    private final Map<Long, Item> items = new HashMap<>();
    private Long currentId = 0L;

    public ItemImplService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Item createItem(Item item, Long ownerId) {
        User owner = userService.getUserById(ownerId);
        if (owner == null) {
            throw new EntityNotFoundException("User not found");
        }
        item.setId(currentId);
        item.setOwnerId(ownerId);
        items.put(currentId, item);
        currentId++;
        return item;
    }

    @Override
    public Item updateItem(Long itemId, Item item, Long ownerId) {
        Item existingItem = items.get(itemId);
        if (existingItem == null) {
            throw new ValidationException("Item not found");
        }

        if (!existingItem.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Only owner can update item");
        }

        // Эти проверки уже корректны для PATCH
        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }

        return existingItem;
    }

    @Override
    public Item getItemById(Long itemId) {
        if (itemId == null) {
            throw new ValidationException("itemId not found");
        }
        Item item = items.get(itemId);
        return item;
    }

    @Override
    public List<Item> getUserItems(Long ownerId) {

        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable())) // ← ИСПРАВЬ ЗДЕСЬ!
                .filter(item ->
                        item.getName().toLowerCase().contains(text.toLowerCase()) ||
                                item.getDescription().toLowerCase().contains(text.toLowerCase())
                ).collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long itemId, Long ownerId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new ValidationException("itemId not found");
        }
        if (!item.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Only owner can delete item");
        }
        items.remove(itemId);
    }
}
