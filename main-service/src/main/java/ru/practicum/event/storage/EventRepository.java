package ru.practicum.event.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer>, CustomEventRepository {
    List<Event> findAllByInitiatorId(Integer userId, Pageable page);

    Event findByIdAndInitiatorId(Integer eventId, Integer userId);

    List<Event> findAllByState(EventState state, Pageable page);

    List<Event> findAllByCategoryId(Integer catId);
}
