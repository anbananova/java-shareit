package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Transactional
    @Override
    public UserDto addUser(User user) {
        checkValidation(user, false);
        User userDb = userRepository.save(user);
        checkEmail(user);
        log.info("Пользователь добавлен в базу данных в таблицу users по ID: {} \n {}", userDb.getId(), userDb);
        return UserMapper.toUserDto(userDb);
    }

    @Transactional
    @Override
    public UserDto updateUser(Long userId, User user) {
        user.setId(userId);
        checkValidation(user, true);
        checkEmail(user);
        User userOld = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        if (user.getName() != null) {
            userOld.setName(user.getName());
        }
        if (user.getEmail() != null) {
            userOld.setEmail(user.getEmail());
        }

        User userUpd = userRepository.save(userOld);
        if (userUpd == null) {
            userUpd = userOld;
        }
        log.info("Пользователь обновлен в базе данных в таблице users по ID: {} \n {}", userId, userUpd);
        return UserMapper.toUserDto(userUpd);
    }

    @Transactional
    @Override
    public UserDto deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        userRepository.deleteById(userId);
        log.info("Пользователь удален из базы данных из таблице users по ID: {} \n {}", userId, user);
        return UserMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден.")));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void checkValidation(User user, boolean emailNull) {
        if (!emailNull && user.getEmail() == null) {
            throw new ValidationException("Email пользователя не может быть пустым");
        }
    }

    private void checkEmail(User user) {
        List<User> usersSameEmail = userRepository.findAllByEmail(user.getEmail()).stream()
                .filter(u -> !Objects.equals(u.getId(), user.getId()))
                .collect(Collectors.toList());

        if (!usersSameEmail.isEmpty()) {
            log.info("Email {} пользователя ID: {} уже есть у пользователей: {}.",
                    user.getEmail(), user.getId(), usersSameEmail);
            throw new ConflictException("Email пользователя не может повторяться.");
        }
    }
}
