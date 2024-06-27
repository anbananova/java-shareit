package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto addUser(UserDto user);

    UserDto updateUser(Long userId, UserDto user);

    UserDto deleteUser(Long userId);

    UserDto getUserById(Long userId);

    List<UserDto> getAllUsers();

    void checkValidation(User user, boolean emailNull);
}
