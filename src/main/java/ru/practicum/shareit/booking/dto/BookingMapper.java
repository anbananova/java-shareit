package ru.practicum.shareit.booking.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        if (booking != null) {
            return BookingDto.builder()
                    .id(booking.getId())
                    .itemId(booking.getItem().getId())
                    .start(booking.getStart())
                    .end(booking.getEnd())
                    .status(booking.getStatus())
                    .item(ItemMapper.toItemDto(booking.getItem()))
                    .booker(UserMapper.toUserDto(booking.getBooker()))
                    .build();
        } else {
            return null;
        }
    }

    public static Booking toBooking(BookingDtoPartial bookingDto, Item item, User user) {
        if (bookingDto != null) {
            return Booking.builder()
                    .id(bookingDto.getId())
                    .start(bookingDto.getStart())
                    .end(bookingDto.getEnd())
                    .item(item)
                    .booker(user)
                    .status(bookingDto.getStatus())
                    .build();
        } else {
            return null;
        }
    }
}
