package ru.practicum.shareit.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDtoItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

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
                    .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
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

    public static Item toItem(ItemDto itemDto, User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner.getId())
                .build();
    }

    public static Item toItemWithRequest(ItemDto dto, User owner, ItemRequest request) {
        return Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner.getId())
                .request(request)
                .build();
    }
}
