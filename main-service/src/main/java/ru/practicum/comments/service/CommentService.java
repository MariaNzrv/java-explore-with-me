package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.storage.CommentRepository;
import ru.practicum.error.exception.ConflictValidationException;
import ru.practicum.error.exception.EntityNotFoundException;
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
//        String text = commentDto.getText();
//        validateText(text);
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
//        String text = commentDto.getText();
//        validateText(text);
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
            throw new EntityNotFoundException("Комментарий с Id = " + commentId + " не существует");
        });
    }

    public List<Comment> findAllCommentsOfUser(Integer userId) {
        return commentRepository.findAllByAuthorId(userId);
    }

    public void deleteCommentByAdmin(Integer commentId) {
        Comment comment = findCommentById(commentId);
        commentRepository.delete(comment);
    }

    public List<Comment> findAllCommentsOfEvent(Integer eventId) {
        return commentRepository.findAllByEventId(eventId);
    }

//    private Event findEventById(Integer eventId) {
//        return eventRepository.findById(eventId).orElseThrow(() -> {
//            log.error("Событие с Id = {} не существует", eventId);
//            throw new EntityNotFoundException("Событие с Id = " + eventId + " не существует");
//        });
//    }

//    private static void validateText(String text) {
//        if (text == null || text.isEmpty() || text.isBlank()) {
//            log.warn("Отсутствует текст комментария");
//            throw new ValidationException("Отсутствует текст комментария");
//        }
//    }
}
