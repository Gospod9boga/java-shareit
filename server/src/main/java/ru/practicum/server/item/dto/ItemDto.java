package ru.practicum.server.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.server.booking.dto.BookingShortDto;
import ru.practicum.server.comments.CommentResponseDto;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private List<CommentResponseDto> comments = new ArrayList<>();
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
}