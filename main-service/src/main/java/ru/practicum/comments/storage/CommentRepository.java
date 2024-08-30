package ru.practicum.comments.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comments.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findAllByAuthorId(Integer userId);
    List<Comment> findAllByEventId(Integer eventId);
}
