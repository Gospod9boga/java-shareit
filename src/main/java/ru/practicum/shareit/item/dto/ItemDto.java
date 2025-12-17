package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comments.CommentResponseDto;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
public class ItemDto {

    private Long id;

    @NotBlank(groups = {Create.class})
    private String name;

    @NotBlank(groups = {Create.class})
    private String description;

    @NotNull(groups = {Create.class}, message = "Поле available обязательно")
    private Boolean available;

    private Long requestId;

    public Object getOwnerId() {
        return null;
    }

    private List<CommentResponseDto> comments;

    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;

    public interface Create {
    }

    public interface Update {
    }
}
