package ru.practicum.server.request.dto.mapper;

import ru.practicum.server.item.model.Item;
import ru.practicum.server.request.ItemRequest;
import ru.practicum.server.request.dto.ItemForRequestDto;
import ru.practicum.server.request.dto.ItemRequestDto;
import ru.practicum.server.request.dto.ItemRequestResponseDto;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemForRequestDto toItemForRequestDto(Item item){
       ItemForRequestDto dto = new ItemForRequestDto();
       dto.setId(item.getId());
       dto.setName(item.getName());
       dto.setOwnerId(item.getOwner().getId());
       return dto;

    }

    public static ItemRequestResponseDto toDto(ItemRequest itemRequest) {
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(itemRequest.getId());
        dto.setDescription(itemRequest.getDescription());
        dto.setCreated(itemRequest.getCreated());

        List<ItemForRequestDto> itemDtos = itemRequest.getItems().stream()
                .map(ItemRequestMapper::toItemForRequestDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }

    public static ItemRequest toEntity(ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        return itemRequest;
    }
}
