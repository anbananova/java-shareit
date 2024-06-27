package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.booking.dto.BookingDtoItem;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder()
public class ItemDtoExtra {
    @EqualsAndHashCode.Exclude
    private Long id;
    @NotBlank(message = "Название вещи не может быть пустой.")
    private String name;
    @NotBlank(message = "Название вещи не может быть пустой.")
    private String description;
    @NotNull(message = "Достпуность не может быть пустая.")
    private Boolean available;
    private BookingDtoItem lastBooking;
    private BookingDtoItem nextBooking;
    private List<CommentDto> comments;
}
