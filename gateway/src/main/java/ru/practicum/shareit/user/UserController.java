package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.utilities.Create;
import ru.practicum.shareit.utilities.Update;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> addUser(@Validated(Create.class) @RequestBody UserDto user) {
        log.info("Получен POST запрос на добавление нового пользователя: {}", user);
        return userClient.addUser(user);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.info("Получен GET запрос на нахождение всех пользователей");
        return userClient.getAllUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        log.info("Получен GET запрос на нахождение пользователя по ID: {}", userId);
        return userClient.getUserById(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId,
                                             @Validated(Update.class) @RequestBody UserDto user) {
        log.info("Получен PATCH запрос на обновление пользователя. Было:\n{}\n Стало:\n {}",
                userClient.getUserById(userId), user);
        return userClient.updateUser(userId, user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long userId) {
        log.info("Получен DELETE запрос на удаление пользователя по ID: {}", userId);
        return userClient.deleteUser(userId);
    }

}
