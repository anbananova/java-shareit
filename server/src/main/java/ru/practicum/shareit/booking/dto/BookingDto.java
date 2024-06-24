package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingDto {
    @EqualsAndHashCode.Exclude
    private Long id;
    private Long itemId;
    @FutureOrPresent
    @NotNull(message = "Дата начала не может быть пустая.")
    private LocalDateTime start;
    @FutureOrPresent
    @NotNull(message = "Дата окончания не может быть пустая.")
    private LocalDateTime end;
    private BookingStatus status;
    private ItemDto item;
    private UserDto booker;
}
