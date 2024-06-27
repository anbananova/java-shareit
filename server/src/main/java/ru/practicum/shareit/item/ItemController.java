package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoPartial;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoExtra;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utilities.Create;
import ru.practicum.shareit.utilities.Update;

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
    public ItemDto addItem(@Validated(Create.class) @RequestBody ItemDto item,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен POST запрос на добавление новой вещи: {}, пользователем: {}", item, userId);
        return itemService.addItem(item, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @Validated(Update.class) @RequestBody Item item,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен PATCH запрос на обновление вещи пользователем: {}. Было:\n{}\n Стало:\n {}",
                userId, itemService.getItemById(itemId, userId), item);
        return itemService.updateItem(itemId, item, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDtoExtra getItemById(@PathVariable Long itemId,
                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на нахождение вещи по ID: {}", itemId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDtoExtra> getItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @RequestParam(required = false, defaultValue = "0") final Integer from,
                                       @RequestParam(required = false, defaultValue = "10") final Integer size) {
        log.info("Получен GET запрос на нахождение всех вещей пользователя: {} с параметрами from={} & size= {}.",
                userId, from, size);
        int page = from > 0 ? from / size : from;
        return itemService.getAllItems(userId, PageRequest.of(page, size));
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(defaultValue = "") String text,
                                     @RequestParam(required = false, defaultValue = "0") final Integer from,
                                     @RequestParam(required = false, defaultValue = "10") final Integer size) {
        log.info("Получен GET запрос на поиск всех вещей с текстом: {} с параметрами from={} & size= {}.",
                text, from, size);
        int page = from > 0 ? from / size : from;
        return itemService.searchItems(text, PageRequest.of(page, size));
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody @Valid CommentDtoPartial comment) {
        log.info("Получен POST запрос на добавление комментария к вещи: {}, пользователем: {}, текст комментария: {}.", itemId, userId, comment);
        return itemService.addComment(itemId, userId, comment);
    }
}
