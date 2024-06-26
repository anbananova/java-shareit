package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoPartial;
import ru.practicum.shareit.booking.service.BookingService;
import org.springframework.data.domain.PageRequest;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto addBooking(@Valid @RequestBody BookingDtoPartial booking,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен POST запрос на добавление нового бронирования: {}, пользователем: {}", booking, userId);
        return bookingService.addBooking(booking, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(@PathVariable Long bookingId,
                                    @RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestParam Boolean approved) {
        log.info("Получен PATCH запрос на обновление бронирования пользователем: {}. Было:\n{}\n Стало:\n {}",
                bookingId, bookingService.getBookingById(bookingId).getStatus(), approved);
        return bookingService.updateBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на нахождение бронирования по ID: {} пользователем по ID: {}", bookingId, userId);
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam(defaultValue = "ALL") String state,
                                        @RequestParam(required = false, defaultValue = "0") final Integer from,
                                        @RequestParam(required = false, defaultValue = "10") final Integer size) {
        log.info("Получен GET запрос на нахождение всех бронирований пользователя: {} в статусе: {} " +
                "с параметрами from={} & size= {}.", userId, state, from, size);
        int page = from > 0 ? from / size : from;
        return bookingService.getAllBookings(userId, state, PageRequest.of(page, size));
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(defaultValue = "ALL") String state,
                                             @RequestParam(required = false, defaultValue = "0") final Integer from,
                                             @RequestParam(required = false, defaultValue = "10") final Integer size) {
        log.info("Получен GET запрос на нахождение всех бронирований пользователя: {} в статусе: {} " +
                "с параметрами from={} & size= {}.", userId, state, from, size);
        int page = from > 0 ? from / size : from;
        return bookingService.getAllBookingsOwner(userId, state, PageRequest.of(page, size));
    }
}
