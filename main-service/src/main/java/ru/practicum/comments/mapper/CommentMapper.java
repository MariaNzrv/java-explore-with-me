package ru.practicum.comments.mapper;

import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentMapper {
    public static CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getEvent().getId(),
                comment.getCreated(),
                comment.getLastUpdated()
        );
    }

    public static List<CommentDto> toDto(List<Comment> comments) {
        List<CommentDto> commentDtos = new ArrayList<>();
        for (Comment comment : comments) {
            commentDtos.add(toDto(comment));
        }
        return commentDtos;
    }

    public static Comment toComment(CommentDto commentDto) {
        return new Comment(
                commentDto.getText());
    }

    public static CommentDto toDtoForEvent(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setCreated(comment.getCreated());
        commentDto.setLastUpdated(comment.getLastUpdated());
        return commentDto;
    }

    public static List<CommentDto> toDtoForEvent(List<Comment> comments) {
        List<CommentDto> commentDtos = new ArrayList<>();
        for (Comment comment : comments) {
            commentDtos.add(toDtoForEvent(comment));
        }
        return commentDtos;
    }
}
