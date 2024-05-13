package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserInMemoryStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserInMemoryStorageTest {
    private UserStorage userStorage;

    @BeforeEach
    public void newUserStorage() {
        this.userStorage = new UserInMemoryStorage();
    }

    private User makeUserWithoutId() {
        return User.builder()
                .name("userName")
                .email("userEmail@mail.ru")
                .build();
    }

    @Test
    void testAddUser_ShouldSaveUser_WhenUserIsNotNull() {
        //given
        User user = makeUserWithoutId();

        //do
        Long userId = userStorage.addUser(user).getId();
        User savedUser = userStorage.getUserById(userId).orElse(null);

        //expect
        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void testDeleteUser_ShouldDeleteUserId3_WhenUserIsNotNullAndExistInTable() {
        //given
        User user1 = makeUserWithoutId();
        User user2 = makeUserWithoutId();
        user2.setEmail("test@mail.test");
        user2.setName("foo");
        User user3 = makeUserWithoutId();
        user3.setEmail("test2@mail.test");
        user3.setName("bar");

        //do
        userStorage.addUser(user1);
        userStorage.addUser(user2);
        userStorage.addUser(user3);
        userStorage.deleteUser(user3);
        //expect
        assertEquals(2, userStorage.getAllUsers().size(), "Количество записей больше 2х");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userStorage.getUserById(user3.getId()), "Исключение не выброшено");
        assertEquals("Такого пользователя с id = " + user3.getId() + " не существует.", exception.getMessage());
    }

    @Test
    void testUpdateUser_ShouldChangeNewUserNameBySameUserId_WhenUserIsNotNull() {
        //given
        User user = makeUserWithoutId();
        Long userId = userStorage.addUser(user).getId();

        User userForUpdate = makeUserWithoutId();
        userForUpdate.setId(userId);
        userForUpdate.setName("UpdatedName");

        //do
        userStorage.updateUser(userForUpdate);
        User updatedUser = userStorage.getUserById(userId).orElse(null);

        //expect
        assertThat(updatedUser)
                .isNotNull()
                .isNotEqualTo(user);

        assertEquals("UpdatedName", updatedUser.getName(), "Имя пользователя не совпадает с обновленным");
    }

    @Test
    void testGetUserById_ShouldReturnSavedUser_WhenUserIsNotNull() {
        //given
        User user = makeUserWithoutId();
        Long userId = userStorage.addUser(user).getId();

        //do
        User savedUser = userStorage.getUserById(userId).orElse(null);
        //expect
        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void testGetAllUsers_ShouldReturnListOf3SavedUsers_WhenUsersAreNotNull() {
        //given
        User user1 = makeUserWithoutId();
        User user2 = makeUserWithoutId();
        user2.setEmail("foo@test.com");
        user2.setName("foo");
        User user3 = makeUserWithoutId();
        user3.setEmail("bar@test.com");
        user3.setName("bar");

        userStorage.addUser(user1);
        userStorage.addUser(user2);
        userStorage.addUser(user3);
        List<User> users = List.of(user1, user2, user3);

        //do
        List<User> savedUsers = userStorage.getAllUsers();

        //expect
        assertThat(savedUsers)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(users);
        assertEquals(3, users.size(), "Размер списка не совпадает");
    }
}
