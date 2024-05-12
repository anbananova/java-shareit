package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(@Qualifier("userInMemoryStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }


    @Override
    public UserDto addUser(User user) {
        checkValidation(user);
        return UserMapper.toUserDto(userStorage.addUser(user));
    }

    @Override
    public UserDto updateUser(Long userId, User user) {
        user.setId(userId);
        checkValidation(user);
        return UserMapper.toUserDto(userStorage.updateUser(user));
    }

    @Override
    public UserDto deleteUser(Long userId) {
        return UserMapper.toUserDto(userStorage.deleteUser(userStorage.getUserById(userId).orElse(null)));
    }

    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(userStorage.getUserById(userId).orElse(null));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userStorage.getAllUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void checkValidation(User user) {
        List<UserDto> usersSameEmail = getAllUsers().stream()
                .filter(u -> u.getEmail().equals(user.getEmail()) && !Objects.equals(u.getId(), user.getId()))
                .collect(Collectors.toList());

        if (!usersSameEmail.isEmpty()) {
            log.info("Email {} пользователя ID: {} уже есть у пользователей: {}.",
                    user.getEmail(), user.getId(), usersSameEmail);
            throw new ConflictException("Email пользователя не может повторяться.");
        }
    }
}
