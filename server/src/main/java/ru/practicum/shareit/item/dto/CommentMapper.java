package ru.practicum.shareit.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Comment;

@UtilityClass
public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        if (comment != null) {
            return CommentDto.builder()
                    .id(comment.getId())
                    .text(comment.getText())
                    .authorName(comment.getAuthor().getName())
                    .created(comment.getCreated())
                    .build();
        } else {
            return null;
        }
    }
}
