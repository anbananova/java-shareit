package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserInMemoryStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {
    private UserService userService;

    @BeforeEach
    public void newUserService() {
        UserStorage userStorage = new UserInMemoryStorage();
        this.userService = new UserServiceImpl(userStorage);
    }

    private User makeUserWithoutId() {
        return User.builder()
                .name("userName")
                .email("userEmail@mail.ru")
                .build();
    }

    @Test
    void testCheckValidation_ShouldThrowException_WhenUserAddedWithDupEmail() {
        User user1 = makeUserWithoutId();
        userService.addUser(user1);

        User user2 = makeUserWithoutId();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.checkValidation(user2, false), "Исключение не выброшено");
        assertEquals("Email пользователя не может повторяться.", exception.getMessage());
    }

    @Test
    void testCheckValidation_ShouldThrowException_WhenUserUpdateWithDupEmailFromAnotherUser() {
        User user1 = makeUserWithoutId();
        userService.addUser(user1);

        User user2 = makeUserWithoutId();
        user2.setEmail("newEmail@mail.ru");
        userService.addUser(user2);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUser(user1.getId(), User.builder().email(user2.getEmail()).build()),
                "Исключение не выброшено");
        assertEquals("Email пользователя не может повторяться.", exception.getMessage());
    }
}
