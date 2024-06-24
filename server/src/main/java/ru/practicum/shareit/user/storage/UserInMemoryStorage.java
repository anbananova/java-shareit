package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UserAlreadyExistException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component("userInMemoryStorage")
@Slf4j
public class UserInMemoryStorage implements UserStorage {
    private final List<User> users = new ArrayList<>();
    private long id = 1L;

    @Override
    public User addUser(User user) {
        user.setId(id);
        id++;

        if (users.contains(user)) {
            throw new UserAlreadyExistException("Пользователь уже был добавлен: " + user);
        }

        log.info("Пользователь добавлен в память users по ID: {} \n {}", user.getId(), user);
        users.add(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        User oldUser = getUserById(user.getId()).orElse(null);
        users.remove(oldUser);

        if (user.getName() == null && oldUser.getName() != null) {
            user.setName(oldUser.getName());
        }
        if (user.getEmail() == null && oldUser.getEmail() != null) {
            user.setEmail(oldUser.getEmail());
        }

        log.info("Текущий пользователь: {}", user);
        users.add(user);
        return user;
    }

    @Override
    public User deleteUser(User user) {
        if (users.contains(user)) {
            users.remove(user);
        } else {
            throw new NotFoundException("Такого пользователя не существует: " + user);
        }
        return user;
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.stream().filter(u -> Objects.equals(userId, u.getId())).findFirst()
                .orElseThrow(() -> new NotFoundException("Такого пользователя с id = " + userId + " не существует.")));
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Текущее количество пользователей: {}", users.size());
        return users;
    }
}
