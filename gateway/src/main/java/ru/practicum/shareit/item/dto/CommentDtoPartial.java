package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@Jacksonized
public class CommentDtoPartial {
    @NotBlank
    @EqualsAndHashCode.Exclude
    String text;
}
