package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoPartial;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoExtra;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto item, Long userId);

    ItemDtoExtra getItemById(Long itemId, Long userId);

    ItemDto updateItem(Long itemId, Item item, Long userId);

    List<ItemDtoExtra> getAllItems(Long userId, Pageable pageable);

    List<ItemDto> searchItems(String text, Pageable pageable);

    void checkValidation(Long userId, ItemDto itemDto, boolean checkEmpty);

    void checkValidation(Long userId);

    CommentDto addComment(Long itemId, Long userId, CommentDtoPartial comment);
}
