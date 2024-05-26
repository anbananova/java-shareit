package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoPartial;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final EntityManager entityManager;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository, ItemRepository itemRepository, EntityManager entityManager) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public BookingDto addBooking(BookingDtoPartial bookingDto, Long userId) {
        checkValidation(bookingDto);

        User booker = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> new NotFoundException("Вещь не найдена."));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования: " + item);
        }
        if (item.getOwner().equals(userId)) {
            throw new NotFoundException("Владелец вещи не может ее бронировать.");
        }

        bookingDto.setStatus(BookingStatus.WAITING);
        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        Booking bookingDb = bookingRepository.save(booking);
        log.info("Бронирование добавлено в базу данных в таблицу bookings по ID: {} \n {}", bookingDb.getId(), bookingDb);
        return BookingMapper.toBookingDto(bookingDb);
    }

    @Transactional
    @Override
    public BookingDto updateBooking(Long bookingId, Long userId, Boolean approved) {
        Booking bookingOld = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование не найдено."));

        checkOwner(userId, bookingOld.getItem());

        if (bookingOld.getStatus().equals(BookingStatus.APPROVED) && approved) {
            throw new ValidationException("Статус букинга " + bookingId + " уже был одобрен.");
        }

        if (approved) {
            bookingRepository.updateBookingStatusById(bookingId, BookingStatus.APPROVED);
            itemRepository.updateItemAvailableById(bookingOld.getItem().getId(), true);
        } else {
            bookingRepository.updateBookingStatusById(bookingId, BookingStatus.REJECTED);
        }

        Booking bookingUpd = bookingRepository.findById(bookingId).get();
        entityManager.refresh(bookingUpd);
        log.info("Бронирование обновлено в базе данных в таблице bookings по ID: {} \n {}", bookingId, bookingUpd);

        return BookingMapper.toBookingDto(bookingUpd);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование не найдено."));

        User ogBooker = booking.getBooker();
        Item itemForBooking = booking.getItem();

        checkOwnerOrBooker(userId, itemForBooking, ogBooker);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long bookingId) {
        return BookingMapper.toBookingDto(bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование не найдено.")));
    }

    @Override
    public List<BookingDto> getAllBookings(Long userId) {
        return null;
    }

    @Override
    public List<BookingDto> getAllBookings(Long userId, String state) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
        List<Booking> bookings = new ArrayList<>();

        switch (bookingState) {
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId).stream()
                        .filter(b -> b.getEnd().isAfter(LocalDateTime.now()) && b.getStart().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId).stream()
                        .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId).stream()
                        .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case WAITING:
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(bookingState.name()));
                break;
            case ALL:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
        }

        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllBookingsOwner(Long userId, String state) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
        List<Booking> bookings = new ArrayList<>();

        switch (bookingState) {
            case CURRENT:
                bookings = bookingRepository.findAllByOwnerIdOrderByStartDesc(userId).stream()
                        .filter(b -> b.getEnd().isAfter(LocalDateTime.now()) && b.getStart().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case PAST:
                bookings = bookingRepository.findAllByOwnerIdOrderByStartDesc(userId).stream()
                        .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByOwnerIdOrderByStartDesc(userId).stream()
                        .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
                break;
            case WAITING:
            case REJECTED:
                bookings = bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(bookingState.name()));
                break;
            case ALL:
                bookings = bookingRepository.findAllByOwnerIdOrderByStartDesc(userId);
        }

        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public void checkValidation(BookingDtoPartial bookingDto) {
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException("Неверные даты бронирования: " + bookingDto);
        }
    }

    private void checkOwner(Long userId, Item item) {
        if (!userId.equals(item.getOwner())) {
            throw new NotFoundException("Пользователь не является владельцем вещи: " + userId + item);
        }
    }

    private void checkOwnerOrBooker(Long userId, Item item, User booker) {
        if (!userId.equals(item.getOwner()) && !userId.equals(booker.getId())) {
            throw new NotFoundException("Пользователь " + userId + " не является владельцем вещи ( " + item + " ) или автором бронирования ( " + booker + " ).");
        }
    }
}
