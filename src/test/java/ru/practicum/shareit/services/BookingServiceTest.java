package ru.practicum.shareit.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoPartial;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BookingServiceTest {
    @Autowired
    private BookingService bookingService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private EntityManager entityManager;

    @Test
    void testCheckValidation_ShouldReturnError_WhenDatesSame() {
        LocalDateTime date = LocalDateTime.now();

        BookingDtoPartial bookingDto = BookingDtoPartial.builder()
                .itemId(1L)
                .start(date)
                .end(date)
                .build();

        assertThrows(ValidationException.class, () -> bookingService.addBooking(bookingDto, 1L));
    }

    @Test
    void testCheckValidation_ShouldReturnError_WhenEndEarlierStart() {
        LocalDateTime date = LocalDateTime.now();

        BookingDtoPartial bookingDto = BookingDtoPartial.builder()
                .itemId(1L)
                .start(date)
                .end(date.minusDays(1))
                .build();

        assertThrows(ValidationException.class, () -> bookingService.addBooking(bookingDto, 1L));
    }

    @Test
    void testAddBooking_ShouldReturnError_WhenUserNotFound() {
        LocalDateTime date = LocalDateTime.now();

        BookingDtoPartial bookingRequestDTO = BookingDtoPartial.builder()
                .itemId(1L)
                .start(date)
                .end(date.plusDays(1))
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(bookingRequestDTO, 1L));

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testAddBooking_ShouldReturnError_WhenItemNotFound() {
        LocalDateTime date = LocalDateTime.now();

        BookingDtoPartial bookingRequestDTO = BookingDtoPartial.builder()
                .itemId(1L)
                .start(date)
                .end(date.plusDays(1))
                .build();

        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(bookingRequestDTO, user.getId()));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void testAddBooking_ShouldReturnError_WhenBookerIsOwner() {
        LocalDateTime date = LocalDateTime.now();

        BookingDtoPartial bookingRequestDTO = BookingDtoPartial.builder()
                .itemId(1L)
                .start(date)
                .end(date.plusDays(1))
                .build();

        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(bookingRequestDTO, user.getId()));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void testAddBooking_ShouldReturnError_WhenItemNotAvailable() {
        LocalDateTime date = LocalDateTime.now();

        BookingDtoPartial bookingRequestDTO = BookingDtoPartial.builder()
                .itemId(1L)
                .start(date)
                .end(date.plusDays(1))
                .build();

        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.FALSE)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingService.addBooking(bookingRequestDTO, user.getId()));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void testAddBooking_ShouldReturnBooking_WhenBookingOk() {
        LocalDateTime date = LocalDateTime.now();

        BookingDtoPartial bookingRequestDTO = BookingDtoPartial.builder()
                .itemId(1L)
                .start(date)
                .end(date.plusDays(1))
                .build();

        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(date)
                .end(date.plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();


        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.save(any()))
                .thenReturn(booking);

        BookingDto result = bookingService.addBooking(bookingRequestDTO, 2L);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);
        assertThat(result.getItem()).isNotNull();
        assertThat(result.getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void testUpdateBooking_ShouldReturnError_WhenBookingNotFound() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.updateBooking(1L, 1L, Boolean.TRUE));

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void testUpdateBooking_ShouldReturnError_WhenBookingApprovedAlready() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(1L)
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class,
                () -> bookingService.updateBooking(1L, 1L, Boolean.TRUE));

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void testUpdateBooking_ShouldReturnOk_WhenBookingOk() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        BookingDto result = bookingService.updateBooking(1L, 1L, Boolean.TRUE);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);
        assertThat(result.getItem()).isNotNull();
        assertThat(result.getBooker()).isNotNull();

        verify(bookingRepository, times(2)).findById(anyLong());
        verify(itemRepository, times(1)).updateItemAvailableById(anyLong(), anyBoolean());
        verify(bookingRepository, times(1)).updateBookingStatusById(anyLong(), any());
    }

    @Test
    void testGetBookingById_ShouldReturnError_WhenUserNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 1L));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetBookingById_ShouldReturnError_WhenBookingNotFound() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 1L));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetBookingById_ShouldReturnOk_WhenBookingExists() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(1L, 1L);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);

        assertThat(result.getItem()).isNotNull();
        assertThat(result.getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetAllBookingsOwner_ShouldReturnError_WhenUserNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getAllBookingsOwner(1L, "ALL", null));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetAllBookingsOwner_ShouldAllBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookingsOwner(user.getId(), "ALL", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByOwnerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void testGetAllBookingsOwner_ShouldWaitingBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookingsOwner(user.getId(), "WAITING", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void testGetAllBookingsOwner_ShouldRejectedBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.REJECTED)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookingsOwner(user.getId(), "REJECTED", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void testGetAllBookingsOwner_ShouldCurrentBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookingsOwner(user.getId(), "CURRENT", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByOwnerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void testGetAllBookingsOwner_ShouldPastBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookingsOwner(user.getId(), "PAST", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByOwnerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void testGetAllBookingsOwner_ShouldFutureBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookingsOwner(user.getId(), "FUTURE", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByOwnerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void testGetAllBookings_ShouldReturnError_WhenUserNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getAllBookings(1L, "ALL", null));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetAllBookings_ShouldAllBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookings(user.getId(), "ALL", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void testGetAllBookings_ShouldWaitingBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookings(user.getId(), "WAITING", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void testGetAllBookings_ShouldRejectedBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.REJECTED)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookings(user.getId(), "REJECTED", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void testGetAllBookings_ShouldCurrentBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookings(user.getId(), "CURRENT", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void testGetAllBookings_ShouldPastBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookings(user.getId(), "PAST", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void testGetAllBookings_ShouldFutureBooking_WhenBookingsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getAllBookings(user.getId(), "FUTURE", null);
        BookingDto expected = BookingMapper.toBookingDto(booking);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);

        assertThat(result.get(0).getItem()).isNotNull();
        assertThat(result.get(0).getBooker()).isNotNull();

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByStartDesc(anyLong(), any());
    }
}
