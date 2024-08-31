package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.storage.CommentRepository;
import ru.practicum.error.exception.ConflictValidationException;
import ru.practicum.error.exception.EntityNotFoundException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;

    public Comment createComment(Integer userId, Integer eventId, CommentDto commentDto) {
        User user = userService.findUserById(userId);
        Event event = eventService.findEventById(eventId);

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setCreated(LocalDateTime.now());
        comment.setAuthor(user);
        comment.setEvent(event);

        return commentRepository.save(comment);
    }

    public Comment editComment(Integer userId, Integer commentId, CommentDto commentDto) {
        Comment comment = findCommentById(commentId);
        User user = userService.findUserById(userId);

        if (!comment.getAuthor().equals(user)) {
            log.error("Пользователь не являяется автором комментария");
            throw new ConflictValidationException("Пользователь не являяется автором комментария");
        }

        comment.setText(commentDto.getText());
        comment.setLastUpdated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public void deleteComment(Integer userId, Integer commentId) {
        User user = userService.findUserById(userId);
        Comment comment = findCommentById(commentId);
        if (!comment.getAuthor().equals(user) && !comment.getEvent().getInitiator().equals(user)) {
            log.error("Недостаточно прав");
            throw new ConflictValidationException("Недостаточно прав");
        }
        commentRepository.delete(comment);
    }

    public Comment findCommentById(Integer commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> {
            log.error("Комментарий с Id = {} не существует", commentId);
            return new EntityNotFoundException("Комментарий с Id = " + commentId + " не существует");
        });
    }

    public List<Comment> findAllCommentsOfUser(Integer userId, Integer from, Integer size) {
        Pageable pageable = getPageable(from, size);
        return commentRepository.findAllByAuthorId(userId, pageable);
    }

    public void deleteCommentByAdmin(Integer commentId) {
        Comment comment = findCommentById(commentId);
        commentRepository.delete(comment);
    }

    public List<Comment> findAllCommentsOfEvent(Integer eventId) {
        return commentRepository.findAllByEventId(eventId);
    }

    private Pageable getPageable(Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            log.error("Некорректные значения параметров from = {}, size={}", from, size);
            throw new ValidationException("Некорректные значения параметров from/size");
        }

        Sort sortByEnd = Sort.by(Sort.Direction.ASC, "id");
        return PageRequest.of(from / size, size, sortByEnd);
    }
}
