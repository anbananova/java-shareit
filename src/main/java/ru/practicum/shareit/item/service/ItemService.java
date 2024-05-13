package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Item item, Long userId);

    ItemDto getItemById(Long itemId, Long userId);

    ItemDto getItemById(Long itemId);

    ItemDto updateItem(Long itemId, Item item, Long userId);

    List<ItemDto> getAllItems(Long userId);

    List<ItemDto> searchItems(String text);

    void checkValidation(Long userId, Item item, boolean checkEmpty);

    void checkValidation(Long userId);
}
