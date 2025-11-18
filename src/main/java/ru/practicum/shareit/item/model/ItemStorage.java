package ru.practicum.shareit.item.model;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private Long currentId = 0L;

    public Item save(Item item){
        if(item.getId()==null){
            item.setId(++currentId);
        }
        items.put(item.getId(),item);
        return  item;
    }
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }


    public List<Item> findByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> ownerId.equals(item.getOwnerId()))
                .collect(Collectors.toList());
    }

    public List<Item> findAvailableItemsWithText(String text) {
        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName().toLowerCase().contains(lowerText) ||
                        item.getDescription().toLowerCase().contains(lowerText))
                .collect(Collectors.toList());
    }


    public void deleteById(Long itemId) {
    }
}
