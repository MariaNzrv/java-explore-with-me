package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.service.CommentService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/comments")
public class PrivateCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> findAllCommentsOfUser(
            @PathVariable @NonNull @Min(1) Integer userId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return CommentMapper.toDto(commentService.findAllCommentsOfUser(userId, from, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @NonNull @Min(1) Integer userId, @RequestParam @NonNull @Min(1) Integer eventId, @Valid @RequestBody CommentDto commentDto) {
        Comment savedComment = commentService.createComment(userId, eventId, commentDto);
        return CommentMapper.toDto(savedComment);
    }

    @PatchMapping("/{commentId}")
    public CommentDto editComment(@PathVariable @NonNull @Min(1) Integer userId, @PathVariable @NonNull @Min(1) Integer commentId, @RequestBody CommentDto commentDto) {
        return CommentMapper.toDto(commentService.editComment(userId, commentId, commentDto));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @NonNull @Min(1) Integer userId, @PathVariable @NonNull @Min(1) Integer commentId) {
        commentService.deleteComment(userId, commentId);
    }
}
