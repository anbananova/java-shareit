package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoPartial;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(BookingDtoPartial booking, Long userId);

    BookingDto updateBooking(Long bookingId, Long userId, Boolean approved);

    BookingDto getBookingById(Long bookingId, Long userId);

    BookingDto getBookingById(Long bookingId);

    List<BookingDto> getAllBookings(Long userId, String state, Pageable pageable);

    List<BookingDto> getAllBookingsOwner(Long userId, String state, Pageable pageable);

    void checkValidation(BookingDtoPartial bookingDto);
}
