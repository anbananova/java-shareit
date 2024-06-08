package ru.practicum.shareit.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ItemServiceTest {
    @Autowired
    private ItemService itemService;
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CommentRepository commentRepository;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private ItemRequestRepository itemRequestRepository;

    @Test
    void testAddItem_ShouldReturnError_WhenOwnerNotFound() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(Boolean.TRUE)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addItem(itemDto, 1L));
        verify(userRepository, times(1)).findById(any());
    }

    @Test
    void testAddItem_ShouldReturnItemWithRequest_WhenItemAndRequestExist() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(Boolean.TRUE)
                .build();

        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .requester(user)
                .build();

        itemDto.setRequestId(itemRequest.getId());
        item.setRequest(itemRequest);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto result = itemService.addItem(itemDto, user.getId());
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(ItemMapper.toItemDto(item));

        verify(userRepository, times(2)).findById(any());
        verify(itemRequestRepository, times(1)).findById(any());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void testAddItem_ShouldReturnItemWithoutRequest_WhenItemExist() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(Boolean.TRUE)
                .build();

        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto result = itemService.addItem(itemDto, user.getId());
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(ItemMapper.toItemDto(item));

        verify(userRepository, times(2)).findById(any());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void testUpdateItem_ShouldReturnError_WhenOwnerAnother() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        when(itemRepository.findByIdAndOwner(anyLong(), anyLong()))
                .thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(item.getId(), item, 666L));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetItemById_ShouldReturnError_WhenItemNotFound() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(1L, 1L));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetItemById_ShouldReturnItem_WhenItemExists() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("test")
                .item(item)
                .author(user)
                .build();

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdAndOwnerId(anyLong(), anyLong()))
                .thenReturn(List.of(booking));
        when(commentRepository.findAllByItemId(anyLong()))
                .thenReturn(List.of(comment));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        ItemDtoExtra result = itemService.getItemById(1L, 1L);
        ItemDtoExtra expected = ItemMapper.toItemDto(item, booking, null, List.of(CommentMapper.toCommentDto(comment)));

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);

        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getLastBooking()).isNull();

        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getId()).isEqualTo(comment.getId());
        assertThat(result.getComments().get(0).getText()).isEqualTo(comment.getText());
        assertThat(result.getComments().get(0).getAuthorName()).isEqualTo(comment.getAuthor().getName());

        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByItemIdAndOwnerId(anyLong(), anyLong());
        verify(commentRepository, times(1)).findAllByItemId(anyLong());
    }

    @Test
    void testGetItemById_ShouldReturnItemWithBooking_WhenItemAndBookingExists() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();

        Booking booking2 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("test")
                .item(item)
                .author(user)
                .build();

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdAndOwnerId(anyLong(), anyLong()))
                .thenReturn(List.of(booking1, booking2));
        when(commentRepository.findAllByItemId(anyLong()))
                .thenReturn(List.of(comment));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        ItemDtoExtra result = itemService.getItemById(1L, 1L);
        ItemDtoExtra expected = ItemMapper.toItemDto(item, booking1, booking2, List.of(CommentMapper.toCommentDto(comment)));

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);

        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getLastBooking()).isNotNull();

        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getId()).isEqualTo(comment.getId());
        assertThat(result.getComments().get(0).getText()).isEqualTo(comment.getText());
        assertThat(result.getComments().get(0).getAuthorName()).isEqualTo(comment.getAuthor().getName());

        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByItemIdAndOwnerId(anyLong(), anyLong());
        verify(commentRepository, times(1)).findAllByItemId(anyLong());
    }

    @Test
    void testGetAllItems_ShouldReturnItems_WhenItemsExist() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("test")
                .item(item)
                .author(user)
                .build();

        when(itemRepository.findAllByOwner(anyLong(), any()))
                .thenReturn(List.of(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdAndOwnerId(anyLong(), anyLong()))
                .thenReturn(List.of(booking));
        when(commentRepository.findAllByItemId(anyLong()))
                .thenReturn(List.of(comment));

        List<ItemDtoExtra> result = itemService.getAllItems(user.getId(), null);
        ItemDtoExtra resultItemDTO = result.get(0);

        ItemDtoExtra expected = ItemMapper.toItemDto(item, booking, null, List.of(CommentMapper.toCommentDto(comment)));

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(resultItemDTO).isEqualTo(expected);

        assertThat(resultItemDTO.getNextBooking()).isNotNull();
        assertThat(resultItemDTO.getLastBooking()).isNull();
        assertThat(resultItemDTO.getComments()).hasSize(1);
        assertThat(resultItemDTO.getComments().get(0).getId()).isEqualTo(comment.getId());
        assertThat(resultItemDTO.getComments().get(0).getText()).isEqualTo(comment.getText());
        assertThat(resultItemDTO.getComments().get(0).getAuthorName()).isEqualTo(
                comment.getAuthor().getName());

        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByOwner(anyLong(), any());
        verify(bookingRepository, times(1)).findAllByItemIdAndOwnerId(anyLong(), anyLong());
        verify(commentRepository, times(1)).findAllByItemId(anyLong());

    }

    @Test
    void testSearchItems_ShouldReturnEmpty_WhenTextEmpty() {
        List<ItemDto> result = itemService.searchItems("", null);
        assertThat(result).isEmpty();
    }

    @Test
    void testSearchItems_ShouldReturnItem_WhenTextExists() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        when(itemRepository.searchItems(any(), any()))
                .thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("test", null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        assertThat(result.get(0)).isEqualTo(ItemMapper.toItemDto(item));

        verify(itemRepository, times(1)).searchItems(any(), any());
    }

    @Test
    void testAddComment_ShouldReturnError_WhenBookingNotFound() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        when(bookingRepository.findAllByItemIdAndBookerId(anyLong(), anyLong(), any()))
                .thenReturn(Collections.emptyList());
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        assertThrows(ValidationException.class,
                () -> itemService.addComment(item.getId(), user.getId(), CommentDtoPartial.builder()
                        .text("test")
                        .build()));
    }

    @Test
    void testAddComment_ShouldReturnComment_WhenCommentExists() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@test.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test")
                .description("test")
                .owner(user.getId())
                .available(Boolean.TRUE)
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("test")
                .item(item)
                .author(user)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findAllByItemIdAndBookerId(anyLong(), anyLong(), any()))
                .thenReturn(List.of(booking));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(commentRepository.save(any()))
                .thenReturn(comment);

        CommentDto result = itemService.addComment(item.getId(), user.getId(), CommentDtoPartial.builder()
                .text("test")
                .build());

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(CommentMapper.toCommentDto(comment));

        verify(itemRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).save(any());
        verify(bookingRepository, times(1)).findAllByItemIdAndBookerId(anyLong(), anyLong(), any());
    }
}
