package ru.practicum.shareit.item.model;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.AccessDeniedException;
import ru.practicum.shareit.Exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ItemImplService implements ItemService {
    private final UserService userService;
    private final ItemStorage itemStorage;

    public ItemImplService(UserService userService, ItemStorage itemStorage) {
        this.userService = userService;
        this.itemStorage = itemStorage;
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        UserDto owner = userService.getUserById(ownerId);

        Item item = ItemMapper.toEntity(itemDto);
        item.setOwnerId(ownerId);
        Item savedItem = itemStorage.save(item);
        return ItemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemStorage.findById(itemId)
                .orElseThrow(() -> new ValidationException("Item not found"));

        if (!existingItem.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Only owner can update item");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemStorage.save(existingItem);
        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        if (itemId == null) {
            throw new ValidationException("itemId not found");
        }
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new ValidationException("Item not found"));
        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getUserItems(Long ownerId) {
        List<Item> userItems = itemStorage.findByOwnerId(ownerId);
        return userItems.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<Item> foundItems = itemStorage.findAvailableItemsWithText(text);
        return foundItems.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long itemId, Long ownerId) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new ValidationException("Item not found"));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Only owner can delete item");
        }
        itemStorage.deleteById(itemId);
    }

}
