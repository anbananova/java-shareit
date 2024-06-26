package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository,
                           ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Transactional
    @Override
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        checkValidation(userId, itemDto, true);
        Item item;
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с id = " + itemDto.getRequestId() + "не найден"));

            item = ItemMapper.toItemWithRequest(itemDto, user, itemRequest);
        } else {
            item = ItemMapper.toItem(itemDto, user);
        }
        item.setOwner(userId);
        Item itemDb = itemRepository.save(item);
        log.info("Вещь добавлена в базу данных в таблицу items по ID: {} \n {}", itemDb.getId(), itemDb);
        return ItemMapper.toItemDto(itemDb);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDtoExtra getItemById(Long itemId, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        List<Booking> bookings = bookingRepository.findAllByItemIdAndOwnerId(itemId, userId);
        log.info("getItemById: for item {} all bookings: {}", itemId, bookings);

        LocalDateTime now = LocalDateTime.now();
        log.info("getItemById: now: {}", now);

        Booking nextBooking;
        Booking lastBooking;
        if (bookings.size() == 1) {
            nextBooking = null;
            lastBooking = getNextBooking(bookings);
        } else {
            nextBooking = getNextBooking(bookings);
            lastBooking = getLastBooking(bookings);
        }

        if (lastBooking != null) {
            log.info("getItemById: last booking id: {}; start: {}; last booking end: {}",
                    lastBooking.getId(), lastBooking.getStart(), lastBooking.getEnd());
        } else {
            log.info("getItemById: last booking null");
        }
        if (nextBooking != null) {
            log.info("getItemById: next booking id: {}; start: {}; next booking end: {}",
                    nextBooking.getId(), nextBooking.getStart(), nextBooking.getEnd());
        } else {
            log.info("getItemById: next booking null");
        }

        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        List<CommentDto> commentsDto = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        return ItemMapper.toItemDto(itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена.")), nextBooking, lastBooking, commentsDto);
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long itemId, Item item, Long userId) {
        checkValidation(userId);
        Item itemOld = itemRepository.findByIdAndOwner(itemId, userId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена."));
        if (item.getName() != null) {
            itemOld.setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemOld.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemOld.setAvailable(item.getAvailable());
        }
        Item itemUpd = itemRepository.save(itemOld);
        log.info("Вещь обновлена в базе данных в таблице items по ID: {} \n {}", itemId, itemUpd);
        return ItemMapper.toItemDto(itemUpd);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDtoExtra> getAllItems(Long userId, Pageable pageable) {
        checkValidation(userId);
        return itemRepository.findAllByOwner(userId, pageable)
                .stream()
                .map(item -> getItemById(item.getId(), userId))
                .sorted(Comparator.comparing(ItemDtoExtra::getId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchItems(String text, Pageable pageable) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        } else {
            return itemRepository.searchItems(text, pageable)
                    .stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void checkValidation(Long userId, ItemDto item, boolean checkEmpty) {
        if (checkEmpty && ((item.getName() == null || item.getName().isEmpty())
                || (item.getDescription() == null || item.getDescription().isEmpty())
                || item.getAvailable() == null)) {
            throw new ValidationException("Поля Item не заполнены.");
        }

        if (userId == null) {
            throw new ValidationException("Пользователь пустой.");
        }
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
    }

    @Override
    public void checkValidation(Long userId) {
        if (userId == null) {
            throw new ValidationException("Пользователь пустой.");
        }
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
    }

    @Transactional
    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDtoPartial comment) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена."));
        List<Booking> booking = bookingRepository.findAllByItemIdAndBookerId(itemId, userId, BookingStatus.APPROVED);

        if (booking.isEmpty()) {
            throw new ValidationException("Не найдено бронирования вещи: " + itemId + " пользователем: " + userId);
        }

        Comment commentBuild = Comment.builder()
                .author(user)
                .item(item)
                .text(comment.getText())
                .build();
        Comment commentDb = commentRepository.save(commentBuild);
        log.info("Комментарий добавлен в базу данных в таблицу comments по ID: {} \n {}", commentDb.getId(), commentDb);
        return CommentMapper.toCommentDto(commentDb);
    }

    private Booking getNextBooking(List<Booking> bookings) {
        return bookings.stream()
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .sorted(Comparator.comparing(Booking::getEnd))
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now())
                        || (LocalDateTime.now().isAfter(booking.getStart()) && LocalDateTime.now().isBefore(booking.getEnd())))
                .findFirst()
                .orElse(null);
    }

    private Booking getLastBooking(List<Booking> bookings) {
        return bookings.stream()
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now())
                        || (LocalDateTime.now().isAfter(booking.getStart()) && LocalDateTime.now().isBefore(booking.getEnd())))
                .max(Comparator.comparing(Booking::getStart))
                .orElse(null);
    }
}
