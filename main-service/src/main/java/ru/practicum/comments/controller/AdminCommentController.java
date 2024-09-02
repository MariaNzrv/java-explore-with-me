package ru.practicum.comments.controller;

import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.service.CommentService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/comments")
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @NonNull @Min(1) Integer commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}
