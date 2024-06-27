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
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoExtra;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = {ItemController.class, ErrorHandler.class})
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    private ItemService itemService;

    private static final String URL = "http://localhost:8080/items";

    @Test
    void testAddItem_ShouldReturnError_WhenNameEmpty() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .description("description")
                .available(Boolean.FALSE)
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(itemDto)));

        response.andExpect(status().isBadRequest())
                .andExpectAll(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));

    }

    @Test
    void testAddItem_ShouldReturnError_WhenDescriptionEmpty() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .available(Boolean.FALSE)
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(itemDto)));

        response.andExpect(status().isBadRequest())
                .andExpectAll(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));

    }

    @Test
    void testAddItem_ShouldReturnError_WhenAvailableEmpty() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(itemDto)));

        response.andExpect(status().isBadRequest())
                .andExpectAll(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));

    }

    @Test
    void testAddItem_ShouldReturnError_WhenOwnerNotFound() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("дрель")
                .description("description")
                .available(Boolean.TRUE)
                .build();

        when(itemService.addItem(any(), anyLong()))
                .thenThrow(NotFoundException.class);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(mapper.writeValueAsString(itemDto)));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testGetItemById_ShouldReturnError_WhenItemNotFound() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(Boolean.TRUE)
                .build();

        when(itemService.getItemById(anyLong(), anyLong()))
                .thenThrow(NotFoundException.class);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(URL.concat("/{itemId}"), 1L)
                .header("X-Sharer-User-Id", 1L));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testAddItem_ShouldReturnOk_WhenOItemIsOk() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("дрель")
                .description("description")
                .available(Boolean.TRUE)
                .build();

        when(itemService.addItem(any(), anyLong()))
                .thenReturn(itemDto);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("X-Sharer-User-Id", 1L)
                .header("Content-Type", "application/json")
                .content(mapper.writeValueAsString(itemDto)));

        response.andExpect(status().isOk());
    }

    @Test
    void testGetItemById_ShouldReturnOk_WhenItemExists() throws Exception {
        ItemDtoExtra itemDto = ItemDtoExtra.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(Boolean.TRUE)
                .comments(Collections.emptyList())
                .lastBooking(null)
                .nextBooking(null)
                .build();

        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDto);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(URL.concat("/{itemId}"), 1L)
                .header("X-Sharer-User-Id", 1L));

        response.andExpect(status().isOk());
    }

    @Test
    void testGetItems_ShouldReturnOk_WhenItemExists() throws Exception {
        ItemDtoExtra itemDto = ItemDtoExtra.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(Boolean.TRUE)
                .comments(Collections.emptyList())
                .lastBooking(null)
                .nextBooking(null)
                .build();

        when(itemService.getAllItems(anyLong(), any()))
                .thenReturn(List.of(itemDto));

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header("X-Sharer-User-Id", 1L));

        response.andExpect(status().isOk());
    }

    @Test
    void testSearchItems_ShouldReturnOk_WhenItemExists() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(Boolean.TRUE)
                .build();

        when(itemService.searchItems(any(), any()))
                .thenReturn(List.of(itemDto));

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(URL.concat("/search"))
                .param("text", "test")
                .header("X-Sharer-User-Id", 1L));

        response.andExpect(status().isOk());
    }
}
