package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    Item addItem(Item item, Long userId);

    Optional<Item> getItemById(Long itemId, Long userId);

    Optional<Item> getItemById(Long itemId);

    Item updateItem(Item item, Long userId);

    List<Item> getAllItems(Long userId);

    List<Item> searchItems(String text);
}
