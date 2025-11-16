package ru.practicum.shareit.request.dto.mapper;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

public class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest itemRequest) {

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription(itemRequest.getDescription());
        return itemRequestDto;
    }

    public static ItemRequest toEntity(ItemRequestDto itemRequestDto){
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        return itemRequest;
    }
}
