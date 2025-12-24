package ru.practicum.server.item.dto.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.server.Exception.ValidationException;
import ru.practicum.server.item.dto.ItemDto;
import ru.practicum.server.item.model.Item;
import ru.practicum.server.request.ItemRequest;
import ru.practicum.server.request.ItemRequestRepository;

import java.util.ArrayList;

@Component
public class ItemMapper {
    public static ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getIsAvailable());
        itemDto.setComments(new ArrayList<>());
        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }
        return itemDto;
    }

    public static Item toEntity(ItemDto itemDto, ItemRequestRepository requestRepository) {
        if (itemDto == null) {
            return null;
        }
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Поле available обязательно");
        }
        item.setIsAvailable(itemDto.getAvailable());

        if (itemDto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.getRequestId()).orElseThrow(() -> new ValidationException("Запрос с ID " + itemDto.getRequestId() + " не найден"));
            item.setRequest(request);
        }

        return item;
    }

    public static Item toEntity(ItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Поле available обязательно");
        }
        item.setIsAvailable(itemDto.getAvailable());

        return item;
    }
}
