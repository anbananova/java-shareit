package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(@Qualifier("itemInMemoryStorage") ItemStorage itemStorage,
                           @Qualifier("userInMemoryStorage") UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto addItem(Item item, Long userId) {
        checkValidation(userId, item, true);
        return ItemMapper.toItemDto(itemStorage.addItem(item, userId));
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        checkValidation(userId);
        return ItemMapper.toItemDto(itemStorage.getItemById(itemId, userId).orElse(null));
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return ItemMapper.toItemDto(itemStorage.getItemById(itemId).orElse(null));
    }

    @Override
    public ItemDto updateItem(Long itemId, Item item, Long userId) {
        item.setId(itemId);
        checkValidation(userId);
        return ItemMapper.toItemDto(itemStorage.updateItem(item, userId));
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        checkValidation(userId);
        return itemStorage.getAllItems(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        } else {
            return itemStorage.searchItems(text)
                    .stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void checkValidation(Long userId, Item item, boolean checkEmpty) {
        if (checkEmpty && ((item.getName() == null || item.getName().isEmpty())
                || (item.getDescription() == null || item.getDescription().isEmpty())
                || item.getAvailable() == null)) {
            throw new ValidationException("Поля Item не заполнены.");
        }

        if (userId == null) {
            throw new ValidationException("Пользователь пустой.");
        }
        userStorage.getUserById(userId);
    }

    @Override
    public void checkValidation(Long userId) {
        if (userId == null) {
            throw new ValidationException("Пользователь пустой.");
        }
        userStorage.getUserById(userId);
    }
}
