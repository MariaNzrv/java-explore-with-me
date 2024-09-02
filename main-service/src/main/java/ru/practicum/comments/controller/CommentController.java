package ru.practicum.comments.controller;

import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.service.CommentService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/event/{eventId}")
    public List<CommentDto> findAllCommentsOfEvent(@PathVariable @NonNull @Min(1) Integer eventId) {
        return CommentMapper.toDtoForEvent(commentService.findAllCommentsOfEvent(eventId));
    }
}
