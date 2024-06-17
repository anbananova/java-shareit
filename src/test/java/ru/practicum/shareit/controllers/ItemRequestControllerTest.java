package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.exceptionhandler.ErrorHandler;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@ContextConfiguration(classes = {ItemRequestController.class, ErrorHandler.class})
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;
    @MockBean
    private UserService userService;

    private static final String URL = "http://localhost:8080/requests";

    @Test
    void testAddRequest_ShouldReturnError_WhenDescriptionEmpty() throws Exception {

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(ItemRequestDto.builder().build())));

        response.andExpect(status().isBadRequest())
                .andExpectAll(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));
    }

    @Test
    void testAddRequest_ShouldReturnError_WhenUserNotFound() throws Exception {
        when(itemRequestService.addRequest(any(), anyLong()))
                .thenThrow(NotFoundException.class);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("description")
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(itemRequestDto)));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testGetRequestById_ShouldReturnError_WhenUserNotFound() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(NotFoundException.class);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(URL.concat("/{requestId}"), 1L)
                .header("X-Sharer-User-Id", 1L));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testGetRequestById_ShouldReturnError_WhenRequestNotFound() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@mail.ru")
                .build();

        when(userService.addUser(any()))
                .thenReturn(user);
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(NotFoundException.class);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(URL.concat("/{requestId}"), 1L)
                .header("X-Sharer-User-Id", 1L));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testAddRequest_ShouldReturnOk_WhenRequestIsOk() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("test")
                .build();

        when(itemRequestService.addRequest(any(), anyLong()))
                .thenReturn(itemRequestDto);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(itemRequestDto)));

        response.andExpect(status().isOk());
    }
}
