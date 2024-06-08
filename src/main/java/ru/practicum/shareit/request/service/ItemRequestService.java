package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestPartial;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(ItemRequestPartial request, Long userId);

    List<ItemRequestDto> getRequestersRequests(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId, Pageable pageable);

    ItemRequestDto getRequestById(Long requestId, Long userId);
}
