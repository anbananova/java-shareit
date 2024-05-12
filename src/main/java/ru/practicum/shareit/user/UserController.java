package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody User user) {
        log.info("Получен POST запрос на добавление нового пользователя: {}", user);
        return userService.addUser(user);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        log.info("Получен GET запрос на нахождение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("Получен GET запрос на нахождение пользователя по ID: {}", userId);
        return userService.getUserById(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId, @RequestBody User user) {
        log.info("Получен PATCH запрос на обновление пользователя. Было:\n{}\n Стало:\n {}",
                userService.getUserById(userId), user);
        return userService.updateUser(userId, user);
    }

    @DeleteMapping("/{userId}")
    public UserDto deleteUser(@PathVariable Long userId) {
        log.info("Получен DELETE запрос на удаление пользователя по ID: {}", userId);
        return userService.deleteUser(userId);
    }

}
