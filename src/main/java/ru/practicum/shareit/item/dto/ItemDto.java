package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Builder
public class ItemDto {
    @EqualsAndHashCode.Exclude
    private Long id;
    @NotBlank(message = "Название вещи не может быть пустой.")
    private String name;
    @NotBlank(message = "Название вещи не может быть пустой.")
    private String description;
    @NotNull(message = "Достпуность не может быть пустая.")
    private Boolean available;
}
