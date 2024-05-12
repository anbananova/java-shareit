package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
    @EqualsAndHashCode.Exclude
    private Long id;
    @NotNull(message = "Логин/имя не может быть пустым")
    private final String name;
    @NotBlank(message = "email пользователя не должен быть пустой, состоять из пробелов или не определяться (null)")
    @Email(message = "Неверный формат записи почты пользователя")
    private final String email;
}
