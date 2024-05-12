package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.request.ItemRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Builder
public class Item {
    @EqualsAndHashCode.Exclude
    private Long id;
    @NotBlank(message = "Название вещи не может быть пустой.")
    private String name;
    @NotBlank(message = "Описание вещи не может быть пустым.")
    private String description;
    @NotNull(message = "Достпуность не может быть пустая.")
    private Boolean available;
    private Long owner;
    private ItemRequest request;
}
