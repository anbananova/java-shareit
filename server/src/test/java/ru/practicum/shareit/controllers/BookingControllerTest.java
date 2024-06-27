package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoPartial;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptionhandler.ErrorHandler;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;


import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = {BookingController.class, ErrorHandler.class})
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;
    @MockBean
    private UserService userService;

    private static final String URL = "http://localhost:8080/bookings";

    @Test
    void testAddBooking_ShouldReturnError_WhenStartEmpty() throws Exception {
        BookingDtoPartial booking = BookingDtoPartial.builder()
                .itemId(1L)
                .end(LocalDateTime.now().plusDays(2))
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isBadRequest())
                .andExpectAll(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));
    }

    @Test
    void testAddBooking_ShouldReturnError_WhenEndEmpty() throws Exception {
        BookingDtoPartial booking = BookingDtoPartial.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isBadRequest())
                .andExpectAll(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));
    }

    @Test
    void testAddBooking_ShouldReturnError_WhenStartInPast() throws Exception {
        BookingDtoPartial booking = BookingDtoPartial.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isBadRequest())
                .andExpectAll(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));
    }

    @Test
    void testAddBooking_ShouldReturnError_WhenEndInPast() throws Exception {
        BookingDtoPartial booking = BookingDtoPartial.builder()
                .itemId(1L)
                .end(LocalDateTime.now().minusDays(2))
                .start(LocalDateTime.now().plusDays(2))
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isBadRequest())
                .andExpectAll(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));
    }

    @Test
    void testAddBooking_ShouldReturnError_WhenUserNotFound() throws Exception {
        when(bookingService.addBooking(any(), anyLong()))
                .thenThrow(NotFoundException.class);

        BookingDtoPartial booking = BookingDtoPartial.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testUpdateBooking_ShouldReturnError_WhenBookingNotFound() throws Exception {
        when(bookingService.getBookingById(anyLong()))
                .thenThrow(NotFoundException.class);
        when(bookingService.updateBooking(anyLong(), anyLong(), Mockito.anyBoolean()))
                .thenThrow(NotFoundException.class);

        BookingDto booking = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.patch(URL.concat("/{bookingId}"), 1L)
                .param("approved", Boolean.FALSE.toString())
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testGetBookingById_ShouldReturnError_WhenUserNotFound() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(NotFoundException.class);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(URL.concat("/{bookingId}"), 1L)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testGetBookingById_ShouldReturnError_WhenBookingNotFound() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@mail.ru")
                .build();

        when(userService.addUser(any()))
                .thenReturn(user);
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(NotFoundException.class);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(URL.concat("/{bookingId}"), 1L)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testAddBooking_ShouldReturnError_WhenItemNotFound() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@mail.ru")
                .build();

        when(userService.addUser(any()))
                .thenReturn(user);
        when(bookingService.addBooking(any(), anyLong()))
                .thenThrow(NotFoundException.class);

        BookingDtoPartial booking = BookingDtoPartial.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testAddBooking_ShouldReturnOk_WhenBookingOk() throws Exception {
        BookingDtoPartial booking = BookingDtoPartial.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto expected = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        UserDto user = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@mail.ru")
                .build();

        when(userService.addUser(any()))
                .thenReturn(user);
        when(bookingService.addBooking(any(), anyLong()))
                .thenReturn(expected);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isOk());
    }

    @Test
    void testUpdateBooking_ShouldReturnOk_WhenBookingExists() throws Exception {
        BookingDtoPartial booking = BookingDtoPartial.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        BookingDto expected = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(bookingService.getBookingById(anyLong()))
                .thenReturn(expected);
        when(bookingService.updateBooking(anyLong(), anyLong(), any()))
                .thenReturn(expected);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.patch(URL.concat("/{bookingId}"), 1L)
                .param("approved", Boolean.FALSE.toString())
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isOk());
    }

    @Test
    void testGetBookingById_ShouldReturnOk_WhenBookingExists() throws Exception {
        BookingDtoPartial booking = BookingDtoPartial.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        BookingDto expected = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(expected);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(URL.concat("/{bookingId}"), 1L)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isOk());
    }

    @Test
    void testGetBookingsOwner_ShouldReturnOk_WhenBookingExists() throws Exception {
        BookingDtoPartial booking = BookingDtoPartial.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        BookingDto expected = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        when(bookingService.getAllBookingsOwner(anyLong(), any(), any()))
                .thenReturn(List.of(expected));

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(URL.concat("/owner"))
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(booking)));

        response.andExpect(status().isOk());
    }
}
