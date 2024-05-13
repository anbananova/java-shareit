package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    User deleteUser(User user);

    Optional<User> getUserById(Long userId);

    List<User> getAllUsers();
}
