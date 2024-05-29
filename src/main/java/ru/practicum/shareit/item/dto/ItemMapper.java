package ru.practicum.shareit.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDtoItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@UtilityClass
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        if (item != null) {
            return ItemDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .build();
        } else {
            return null;
        }
    }

    public static ItemDtoExtra toItemDto(Item item, Booking nextBooking, Booking lastBooking, List<CommentDto> comments) {
        if (item != null) {
            return ItemDtoExtra.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .nextBooking(nextBooking != null ? BookingDtoItem.builder()
                            .id(nextBooking.getId())
                            .bookerId(nextBooking.getBooker().getId())
                            .build() : null)
                    .lastBooking(lastBooking != null ? BookingDtoItem.builder()
                            .id(lastBooking.getId())
                            .bookerId(lastBooking.getBooker().getId())
                            .build() : null)
                    .comments(comments)
                    .build();
        } else {
            return null;
        }
    }
}
