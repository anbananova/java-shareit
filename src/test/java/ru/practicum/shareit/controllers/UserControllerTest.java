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
import ru.practicum.shareit.exceptionhandler.ErrorHandler;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = {UserController.class, ErrorHandler.class})
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    private static final String URL = "http://localhost:8080/users";

    @Test
    void testAddUser_ShouldReturnError_WhenEmailIncorrect() throws Exception {
        User user = User.builder()
                .name("test")
                .email("incorrect email.ru")
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(objectMapper.writeValueAsString(user)));

        response.andExpect(status().isBadRequest())
                .andExpectAll(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));
    }

    @Test
    void testAddUser_ShouldReturnError_WhenEmailDuplicates() throws Exception {
        when(userService.addUser(Mockito.any()))
                .thenThrow(ConflictException.class);

        User user = User.builder()
                .name("userName")
                .email("email@mail.ru")
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(objectMapper.writeValueAsString(user)));

        response.andExpect(status().isConflict())
                .andExpectAll(result -> assertInstanceOf(ConflictException.class, result.getResolvedException()));
    }

    @Test
    void testUpdateUser_ShouldReturnError_WhenUserNotFound() throws Exception {
        when(userService.updateUser(Mockito.anyLong(), Mockito.any()))
                .thenThrow(NotFoundException.class);

        User user = User.builder()
                .name("userName")
                .email("email@mail.ru")
                .build();

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.patch(URL.concat("/{id}"), 1L)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(objectMapper.writeValueAsString(user)));

        response.andExpect(status().isNotFound())
                .andExpectAll(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    void testAddUser_ShouldReturnOk_WhenUserIsOk() throws Exception {
        User user = User.builder()
                .name("userName")
                .email("email@mail.ru")
                .build();

        UserDto userDto = UserMapper.toUserDto(user);

        when(userService.addUser(Mockito.any()))
                .thenReturn(userDto);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .header("Content-Type", "application/json")
                .header("X-Sharer-User-Id", 1L)
                .content(objectMapper.writeValueAsString(userDto)));

        response.andExpect(status().isOk());
    }
}
