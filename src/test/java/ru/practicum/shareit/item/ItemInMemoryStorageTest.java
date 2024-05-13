package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemInMemoryStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserInMemoryStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemInMemoryStorageTest {
    private ItemStorage itemStorage;
    private User user;

    @BeforeEach
    public void newItemStorage() {
        UserStorage userStorage = new UserInMemoryStorage();
        user = makeUserWithoutId();
        user.setId(userStorage.addUser(user).getId());

        this.itemStorage = new ItemInMemoryStorage();
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
    void testAddItem_ShouldSaveItem_WhenItemIsNotNull() {
        //given
        Item item = makeItemWithoutId();

        //do
        Long itemId = itemStorage.addItem(item, item.getOwner()).getId();
        Item savedItem = itemStorage.getItemById(itemId, item.getOwner()).orElse(null);

        //expect
        assertThat(savedItem)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(item);
    }

    @Test
    void testUpdateItem_ShouldChangeNewItemNameBySameItemId_WhenItemIsNotNull() {
        //given
        Item item = makeItemWithoutId();
        Long itemId = itemStorage.addItem(item, item.getOwner()).getId();

        Item itemForUpdate = makeItemWithoutId();
        itemForUpdate.setId(itemId);
        itemForUpdate.setName("UpdatedName");

        //do
        itemStorage.updateItem(itemForUpdate, item.getOwner());
        Item updateditem = itemStorage.getItemById(itemId, item.getOwner()).orElse(null);

        //expect
        assertThat(updateditem)
                .isNotNull()
                .isNotEqualTo(item);

        assertEquals("UpdatedName", updateditem.getName(), "Имя вещи не совпадает с обновленным");
    }

    @Test
    void testGetItemById_ShouldReturnSavedItem_WhenItemIsNotNull() {
        //given
        Item item = makeItemWithoutId();
        Long itemId = itemStorage.addItem(item, item.getOwner()).getId();

        //do
        Item saveditem = itemStorage.getItemById(itemId, item.getOwner()).orElse(null);
        //expect
        assertThat(saveditem)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(item);
    }

    @Test
    void testGetItemByIdNoUserid_ShouldReturnSavedItem_WhenItemIsNotNull() {
        //given
        Item item = makeItemWithoutId();
        Long itemId = itemStorage.addItem(item, item.getOwner()).getId();

        //do
        Item saveditem = itemStorage.getItemById(itemId).orElse(null);
        //expect
        assertThat(saveditem)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(item);
    }

    @Test
    void testGetAllItems_ShouldReturnListOf3SavedItems_WhenItemsAreNotNull() {
        //given
        Item item1 = makeItemWithoutId();
        Item item2 = makeItemWithoutId();
        item2.setName("item2");
        Item item3 = makeItemWithoutId();
        item3.setName("item3");

        itemStorage.addItem(item1, user.getId());
        itemStorage.addItem(item2, user.getId());
        itemStorage.addItem(item3, user.getId());
        List<Item> items = List.of(item1, item2, item3);

        //do
        List<Item> savedItems = itemStorage.getAllItems(user.getId());

        //expect
        assertThat(savedItems)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(items);
        assertEquals(3, items.size(), "Размер списка не совпадает");
    }

    @Test
    void testSearchItems_ShouldReturnListOf2SavedItems_WhenItemsAreNotNull() {
        //given
        Item item1 = makeItemWithoutId();
        Item item2 = makeItemWithoutId();
        item2.setName("item2");
        Item item3 = makeItemWithoutId();
        item3.setName("item3");

        itemStorage.addItem(item1, user.getId());
        itemStorage.addItem(item2, user.getId());
        itemStorage.addItem(item3, user.getId());
        List<Item> items = List.of(item2, item3);

        //do
        List<Item> savedItems = itemStorage.searchItems("iTEm");

        //expect
        assertThat(savedItems)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(items);
        assertEquals(2, items.size(), "Размер списка не совпадает");
    }
}
