package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@Valid @RequestBody Item item,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен POST запрос на добавление новой вещи: {}, пользователем: {}", item, userId);
        return itemService.addItem(item, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @Valid @RequestBody Item item,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен PATCH запрос на обновление вещи пользователем: {}. Было:\n{}\n Стало:\n {}",
                userId, itemService.getItemById(itemId, userId), item);
        return itemService.updateItem(itemId, item, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на нахождение вещи по ID: {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на нахождение всех вещей пользователя: {}.", userId);
        return itemService.getAllItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(defaultValue = "") String text) {
        log.info("Получен GET запрос на поиск всех вещей с текстом: {}.", text);
        return itemService.searchItems(text);
    }
}
