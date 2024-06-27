package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class BookingDtoPartial {
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
}