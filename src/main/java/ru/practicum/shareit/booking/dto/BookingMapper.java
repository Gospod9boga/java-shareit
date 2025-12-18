package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.user.dto.mapper.UserMapper;

public class BookingMapper {

    public static Booking toEntity(BookingResponseDto dto) {
        Booking booking = new Booking();
        booking.setStartDate(dto.getStart());
        booking.setEndDate(dto.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    public static BookingResponseDto toResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStartDate());
        dto.setEnd(booking.getEndDate());
        dto.setStatus(booking.getStatus());

        if (booking.getItem() != null) {
            dto.setItem(ItemMapper.toDto(booking.getItem()));
        }
        if (booking.getBooker() != null) {
            dto.setBooker(UserMapper.toDto(booking.getBooker()));
        }
        return dto;
    }

    public static BookingShortDto toShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStartDate());
        dto.setEnd(booking.getEndDate());
        dto.setBookerId(booking.getBooker() != null ? booking.getBooker().getId() : null);
        return dto;
    }
}