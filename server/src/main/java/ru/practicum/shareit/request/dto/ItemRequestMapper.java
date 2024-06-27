package ru.practicum.shareit.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        if (itemRequest != null) {
            return ItemRequestDto.builder()
                    .id(itemRequest.getId())
                    .description(itemRequest.getDescription())
                    .created(itemRequest.getCreated())
                    .items(itemRequest.getItems() != null
                            ? itemRequest.getItems().stream().map(ItemMapper::toItemDto).collect(Collectors.toList())
                            : Collections.emptyList())
                    .build();
        } else {
            return null;
        }
    }

    public static ItemRequest toItemRequest(ItemRequestPartial itemRequestPartial, User user) {
        if (itemRequestPartial != null) {
            return ItemRequest.builder()
                    .description(itemRequestPartial.getDescription())
                    .requester(user)
                    .build();
        } else {
            return null;
        }
    }
}
