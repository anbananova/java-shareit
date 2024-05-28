package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    @Override
    public ItemDto addItem(Item item, Long userId) {
        checkValidation(userId, item, true);
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

        Booking nextBooking = getNextBooking(bookings);
        Booking lastBooking = getLastBooking(bookings);

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
    public List<ItemDtoExtra> getAllItems(Long userId) {
        checkValidation(userId);
        return itemRepository.findAllByOwner(userId)
                .stream()
                .map(item -> getItemById(item.getId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        } else {
            return itemRepository.searchItems(text)
                    .stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void checkValidation(Long userId, Item item, boolean checkEmpty) {
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
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now()))
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
