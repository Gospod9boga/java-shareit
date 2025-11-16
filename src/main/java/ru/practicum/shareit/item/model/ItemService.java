package ru.practicum.shareit.item.model;

import java.util.List;

public interface ItemService {
    Item createItem(Item item, Long ownerId);

    Item updateItem(Long itemId, Item item, Long ownerId);

    Item getItemById(Long itemId);

    List<Item> getUserItems(Long ownerId);

    List<Item> searchItems(String text);

    void deleteItem(Long itemId, Long ownerId);


}
