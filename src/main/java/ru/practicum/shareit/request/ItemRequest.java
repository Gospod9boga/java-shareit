package ru.practicum.shareit.request;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ItemRequest {
    private Long id;
    private String description;
    private  Long requesterId;
    private LocalDateTime created;
    private List<Long> items;
}
