package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestPartial;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDto addRequest(@Valid @RequestBody ItemRequestPartial request,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен POST запрос на добавление нового запроса: {}, пользователем: {}", request, userId);
        return itemRequestService.addRequest(request, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getRequestersRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на нахождение всех запросов пользователя: {}", userId);
        return itemRequestService.getRequestersRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(required = false, defaultValue = "0") final Integer from,
                                               @RequestParam(required = false, defaultValue = "10") final Integer size) {
        log.info("Получен GET запрос на нахождение всех запросов другими пользователями с параметрами from={} & size={}.",
                from, size);
        int page = from > 0 ? from / size : from;
        return itemRequestService.getAllRequests(userId, PageRequest.of(page, size));
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable Long requestId,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос на нахождение запроса по ID: {} пользователем по ID: {}", requestId, userId);
        return itemRequestService.getRequestById(requestId, userId);
    }
}
