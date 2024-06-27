package ru.practicum.shareit.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void testAddUser_ShouldReturnError_WhenEmailDuplicates() {
        User user = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .build();

        when(userRepository.save(any()))
                .thenThrow(ConflictException.class);

        assertThrows(ConflictException.class, () -> userService.addUser(UserMapper.toUserDto(user)));
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testAddUser_ShouldReturnUser_WhenUserExists() {
        User user = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .build();

        when(userRepository.save(any()))
                .thenReturn(user);

        UserDto userAdded = UserMapper.toUserDto(user);
        UserDto result = userService.addUser(UserMapper.toUserDto(user));

        assertThat(result).usingRecursiveComparison().isEqualTo(userAdded);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testUpdateUser_ShouldReturnError_WhenUserNotFound() {
        User user = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(1L, UserMapper.toUserDto(user)));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testUpdateUser_ShouldReturnUser_WhenUserSame() {
        User user = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        UserDto userAdded = UserMapper.toUserDto(user);
        UserDto result = userService.updateUser(1L, UserMapper.toUserDto(user));

        assertThat(result).usingRecursiveComparison().isEqualTo(userAdded);
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testUpdateUser_ShouldReturnUser_WhenUserAllFieldsDifferent() {
        User user = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(User.builder()
                        .id(1L)
                        .name("update")
                        .email("update@test.com")
                        .build()));

        when(userRepository.save(any()))
                .thenReturn(user);

        UserDto userAdded = UserMapper.toUserDto(user);
        UserDto result = userService.updateUser(1L, UserMapper.toUserDto(user));

        assertThat(result).usingRecursiveComparison().isEqualTo(userAdded);
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testGetAllUsers_ShouldReturnUser_WhenUserExists() {
        User user = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .build();

        when(userRepository.findAll())
                .thenReturn(List.of(user));

        List<UserDto> usersAdded = List.of(UserMapper.toUserDto(user));
        List<UserDto> result = userService.getAllUsers();

        assertThat(result).usingRecursiveComparison().isEqualTo(usersAdded);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById_ShouldReturnUser_WhenUserExists() {
        User user = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        UserDto userAdded = UserMapper.toUserDto(user);
        UserDto result = userService.getUserById(1L);

        assertThat(result).usingRecursiveComparison().isEqualTo(userAdded);
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetUserById_ShouldReturnError_WhenUserNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testDeleteUser_ShouldUseRep1Time_WhenUserDeleted() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(anyLong());
    }
}
