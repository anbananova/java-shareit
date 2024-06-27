package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.utilities.Create;
import ru.practicum.shareit.utilities.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
    @EqualsAndHashCode.Exclude
    private Long id;
    @NotNull(groups = {Create.class}, message = "Логин/имя не может быть пустым")
    private final String name;
    @NotBlank(groups = {Create.class}, message = "email пользователя не должен быть пустой, состоять из пробелов или не определяться (null)")
    @Email(groups = {Create.class, Update.class}, message = "Неверный формат записи почты пользователя")
    private final String email;
}