package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDtoPartial;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.utilities.Create;
import ru.practicum.shareit.utilities.Update;

import javax.validation.Valid;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@Validated(Create.class) @RequestBody ItemDto item,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен POST запрос на добавление новой вещи: {}, пользователем: {}", item, userId);
        return itemClient.addItem(item, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable Long itemId,
                                             @Validated(Update.class) @RequestBody ItemDto item,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен PATCH запрос на обновление вещи пользователем: {}. Было:\n{}\n Стало:\n {}",
                userId, itemClient.getItemById(itemId, userId), item);
        return itemClient.updateItem(itemId, item, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId,
                                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на нахождение вещи по ID: {}", itemId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(required = false, defaultValue = "0") final Integer from,
                                           @RequestParam(required = false, defaultValue = "10") final Integer size) {
        log.info("Получен GET запрос на нахождение всех вещей пользователя: {} с параметрами from={} & size= {}.",
                userId, from, size);
        return itemClient.getAllItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam(defaultValue = "") String text,
                                              @RequestParam(required = false, defaultValue = "0") final Integer from,
                                              @RequestParam(required = false, defaultValue = "10") final Integer size) {
        log.info("Получен GET запрос на поиск всех вещей с текстом: {} с параметрами from={} & size= {}.",
                text, from, size);
        return itemClient.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                             @RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestBody @Valid CommentDtoPartial comment) {
        log.info("Получен POST запрос на добавление комментария к вещи: {}, пользователем: {}, текст комментария: {}.", itemId, userId, comment);
        return itemClient.addComment(itemId, userId, comment);
    }
}
