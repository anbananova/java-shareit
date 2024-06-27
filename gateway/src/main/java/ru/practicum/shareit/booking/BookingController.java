package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoPartial;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@Valid @RequestBody BookingDtoPartial booking,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен POST запрос на добавление нового бронирования: {}, пользователем: {}", booking, userId);
        return bookingClient.addBooking(booking, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBooking(@PathVariable Long bookingId,
                                                @RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam Boolean approved) {
        log.info("Получен PATCH запрос на обновление бронирования пользователем.");
        return bookingClient.updateBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable Long bookingId,
                                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на нахождение бронирования по ID: {} пользователем по ID: {}", bookingId, userId);
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(defaultValue = "ALL") String state,
                                              @RequestParam(required = false, defaultValue = "0") final Integer from,
                                              @RequestParam(required = false, defaultValue = "10") final Integer size) {
        log.info("Получен GET запрос на нахождение всех бронирований пользователя: {} в статусе: {} " +
                "с параметрами from={} & size= {}.", userId, state, from, size);
        return bookingClient.getAllBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(required = false, defaultValue = "0") final Integer from,
                                                   @RequestParam(required = false, defaultValue = "10") final Integer size) {
        log.info("Получен GET запрос на нахождение всех бронирований пользователя: {} в статусе: {} " +
                "с параметрами from={} & size= {}.", userId, state, from, size);
        return bookingClient.getAllBookingsOwner(userId, state, from, size);
    }
}
