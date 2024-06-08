package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestPartial;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  UserRepository userRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public ItemRequestDto addRequest(ItemRequestPartial request, Long userId) {
        User requester = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        ItemRequest itemRequestDb = itemRequestRepository.save(ItemRequestMapper.toItemRequest(request, requester));
        log.info("Запрос добавлен в базу данных в таблицу requests по ID: {} \n {}", itemRequestDb.getId(), itemRequestDb);
        return ItemRequestMapper.toItemRequestDto(itemRequestDb);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getRequestersRequests(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterId(userId);
        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Pageable pageable) {
        List<ItemRequest> requests = itemRequestRepository.findAllByNotRequesterId(userId, pageable);

        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + requestId + " не найден"));

        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }
}
