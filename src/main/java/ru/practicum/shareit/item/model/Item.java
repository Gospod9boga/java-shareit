package ru.practicum.shareit.item.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Item {
    private String name;
    private String description;
    private Long ownerId;
    private Long id;
    private Boolean available;
    private Long requestId;

}
