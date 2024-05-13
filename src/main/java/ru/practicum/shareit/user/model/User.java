package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;

@Data
@AllArgsConstructor
@Builder
public class User {
    @EqualsAndHashCode.Exclude
    private Long id;
    private String name;
    @Email(message = "Неверный формат записи почты пользователя")
    private String email;
}
