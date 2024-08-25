package ru.practicum.event.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Event;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.model.EventState;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer>, CustomEventRepository {
    List<Event> findAllByInitiatorId(Integer userId, Pageable page);
    Event findByIdAndInitiatorId(Integer eventId, Integer userId);
    List<Event> findAllByState(EventState state, Pageable page);
}
