package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.ItemInMemoryStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserInMemoryStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {
    private ItemService itemService;
    private User user;

    @BeforeEach
    public void newUserService() {
        ItemStorage itemStorage = new ItemInMemoryStorage();
        UserStorage userStorage = new UserInMemoryStorage();
        user = makeUserWithoutId();
        user.setId(userStorage.addUser(user).getId());

        this.itemService = new ItemServiceImpl(itemStorage, userStorage);
    }

    private User makeUserWithoutId() {
        return User.builder()
                .name("userName")
                .email("userEmail@mail.ru")
                .build();
    }

    private Item makeItemWithoutId() {
        return Item.builder()
                .name("Name")
                .description("Description")
                .owner(user.getId())
                .available(true)
                .build();
    }

    @Test
    void testCheckValidation_ShouldThrowException_WhenUserNotExists() {
        Item item = makeItemWithoutId();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addItem(item, 10L), "Исключение не выброшено");
        assertEquals("Такого пользователя с id = " + 10L + " не существует.", exception.getMessage());
    }

    @Test
    void testCheckValidation_ShouldThrowException_WhenUserNull() {
        Item item = makeItemWithoutId();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addItem(item, null), "Исключение не выброшено");
        assertEquals("Пользователь пустой.", exception.getMessage());
    }
}
