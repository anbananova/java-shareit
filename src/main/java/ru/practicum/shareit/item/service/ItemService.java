package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoPartial;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoExtra;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Item item, Long userId);

    ItemDtoExtra getItemById(Long itemId, Long userId);

    ItemDto updateItem(Long itemId, Item item, Long userId);

    List<ItemDtoExtra> getAllItems(Long userId);

    List<ItemDto> searchItems(String text);

    void checkValidation(Long userId, Item item, boolean checkEmpty);

    void checkValidation(Long userId);

    CommentDto addComment(Long itemId, Long userId, CommentDtoPartial comment);
}
