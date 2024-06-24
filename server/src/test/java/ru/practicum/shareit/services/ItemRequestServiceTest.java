package ru.practicum.shareit.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestPartial;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ItemRequestServiceTest {
    @Autowired
    private ItemRequestService itemRequestService;

    @MockBean
    private ItemRequestRepository itemRequestRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testAddRequest_ShouldReturnError_WhenUserNotFound() {
        ItemRequestPartial request = ItemRequestPartial.builder()
                .description("description")
                .build();

        when(userRepository.findById(anyLong()))
                .thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> itemRequestService.addRequest(request, 1L));
        verify(userRepository, times(1)).findById(any());
    }

    @Test
    public void testAddRequest_ShouldReturnRequest_WhenRequestExists() {
        ItemRequestPartial request = ItemRequestPartial.builder()
                .description("description")
                .build();

        User user = User.builder()
                .id(1L)
                .name("name")
                .email("email@mail.ru")
                .build();

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .requester(user)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any()))
                .thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.addRequest(request, user.getId());
        ItemRequestDto expected = ItemRequestMapper.toItemRequestDto(itemRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);

        verify(userRepository, times(1)).findById(any());
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    public void testGetRequestById_ShouldReturnError_WhenUserNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(1L, 1L));

        verify(userRepository, times(1)).findById(any());
    }

    @Test
    public void testGetRequestById_ShouldReturnError_WhenRequestNotFound() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("email@mail.ru")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(1L, user.getId()));

        verify(userRepository, times(1)).findById(any());
        verify(itemRequestRepository, times(1)).findById(any());
    }

    @Test
    public void testGetRequestById_ShouldReturnRequest_WhenRequestExists() {
        User user = User.builder()
                .id(1L)
                .name("max")
                .email("max@mail.ru")
                .build();

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .requester(user)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));

        ItemRequestDto result = itemRequestService.getRequestById(itemRequest.getId(), user.getId());
        ItemRequestDto expected = ItemRequestMapper.toItemRequestDto(itemRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);
        assertThat(result.getItems()).isEmpty();

        verify(userRepository, times(1)).findById(any());
        verify(itemRequestRepository, times(1)).findById(any());
    }

    @Test
    public void testGetAllRequests_ShouldReturnRequest_WhenRequestExists() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("email@mail.ru")
                .build();

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .requester(user)
                .build();

        when(itemRequestRepository.findAllByNotRequesterId(anyLong(), any()))
                .thenReturn(List.of(itemRequest));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(user.getId(), PageRequest.ofSize(1));
        ItemRequestDto expected = ItemRequestMapper.toItemRequestDto(itemRequest);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        assertThat(result.get(0)).isEqualTo(expected);
        assertThat(result.get(0).getItems()).isEmpty();

        verify(itemRequestRepository, times(1)).findAllByNotRequesterId(anyLong(), any());
    }

    @Test
    public void testGetRequestersRequests_ShouldReturnRequest_WhenRequestExists() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("email@mail.ru")
                .build();

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .requester(user)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterId(anyLong()))
                .thenReturn(List.of(itemRequest));

        List<ItemRequestDto> result = itemRequestService.getRequestersRequests(user.getId());
        ItemRequestDto expected = ItemRequestMapper.toItemRequestDto(itemRequest);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);
        assertThat(result.get(0).getItems()).isEmpty();

        verify(userRepository, times(1)).findById(any());
        verify(itemRequestRepository, times(1)).findAllByRequesterId(anyLong());
    }
}
