package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ItemAlreadyExistException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("itemInMemoryStorage")
@Slf4j
public class ItemInMemoryStorage implements ItemStorage {
    private final List<Item> items = new ArrayList<>();
    private long id = 1L;

    @Override
    public Item addItem(Item item, Long userId) {
        item.setId(id);
        id++;
        item.setOwner(userId);

        if (items.contains(item)) {
            throw new ItemAlreadyExistException("Вещь уже была добавлена: " + item);
        }

        log.info("Вещь добавлена в память items по ID: {} \n {}", item.getId(), item);
        items.add(item);
        return item;
    }

    @Override
    public Optional<Item> getItemById(Long itemId, Long userId) {
        return Optional.ofNullable(items.stream()
                .filter(i -> Objects.equals(itemId, i.getId()) && Objects.equals(userId, i.getOwner())).findFirst()
                .orElseThrow(() ->
                        new NotFoundException("Такой вещи с id = " + itemId + " не существует.")));
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return Optional.ofNullable(items.stream()
                .filter(i -> Objects.equals(itemId, i.getId())).findFirst()
                .orElseThrow(() ->
                        new NotFoundException("Такой вещи с id = " + itemId + " не существует.")));
    }

    @Override
    public Item updateItem(Item item, Long userId) {
        Item oldItem = getItemById(item.getId(), userId).orElse(null);
        items.remove(oldItem);

        if (oldItem.getOwner() != null && !oldItem.getOwner().equals(userId)) {
            throw new NotFoundException("Такой вещи с id = " + item.getId() + " у пользователя с id = " + userId + " не существует.");
        }
        if (item.getName() == null && oldItem.getName() != null) {
            item.setName(oldItem.getName());
        }
        if (item.getDescription() == null && oldItem.getDescription() != null) {
            item.setDescription(oldItem.getDescription());
        }
        if (item.getAvailable() == null && oldItem.getAvailable() != null) {
            item.setAvailable(oldItem.getAvailable());
        }

        item.setOwner(oldItem.getOwner());
        log.info("Текущая вещь: {}", item);
        items.add(item);
        return item;
    }

    @Override
    public List<Item> getAllItems(Long userId) {
        List<Item> itemsByUser = items.stream()
                .filter(i -> Objects.equals(i.getOwner(), userId))
                .collect(Collectors.toList());
        log.info("Текущее количество всех вещей: {}, текущее количество всех вещей: {} пользователя: {}",
                items.size(), itemsByUser.size(), userId);
        return itemsByUser;
    }

    @Override
    public List<Item> searchItems(String text) {
        return items.stream()
                .filter(i -> i.getAvailable()
                        && (i.getName().toLowerCase().contains(text.toLowerCase())
                        || i.getDescription().toLowerCase().contains(text.toLowerCase())))
                .collect(Collectors.toList());
    }
}
