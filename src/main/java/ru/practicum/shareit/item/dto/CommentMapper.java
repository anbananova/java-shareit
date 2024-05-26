package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;

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

    public static CommentDtoPartial toCommentDtoPartial(Comment comment) {
        if (comment != null) {
            return CommentDtoPartial.builder()
                    .text(comment.getText())
                    .build();
        } else {
            return null;
        }
    }
}
