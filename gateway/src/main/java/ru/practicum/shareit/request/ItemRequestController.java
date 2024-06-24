package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestPartial;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addRequest(@Valid @RequestBody ItemRequestPartial request,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен POST запрос на добавление нового запроса: {}, пользователем: {}", request, userId);
        return itemRequestClient.addRequest(request, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestersRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на нахождение всех запросов пользователя: {}", userId);
        return itemRequestClient.getRequestersRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(required = false, defaultValue = "0") final Integer from,
                                                 @RequestParam(required = false, defaultValue = "10") final Integer size) {
        log.info("Получен GET запрос на нахождение всех запросов другими пользователями с параметрами from={} & size={}.",
                from, size);
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId,
                                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на нахождение запроса по ID: {} пользователем по ID: {}", requestId, userId);
        return itemRequestClient.getRequestById(requestId, userId);
    }
}
