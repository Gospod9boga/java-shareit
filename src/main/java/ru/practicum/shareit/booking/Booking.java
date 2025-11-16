package ru.practicum.shareit.booking;


import lombok.*;

import java.time.LocalDateTime;

@Data

public class Booking {
    private Long id;
    private Long itemId;
    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
}
