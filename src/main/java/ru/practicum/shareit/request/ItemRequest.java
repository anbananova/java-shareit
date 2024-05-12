package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ItemRequest {
    private final Long id;
    private final String description;
    private final User requestor;
    private final LocalDateTime created;
}
