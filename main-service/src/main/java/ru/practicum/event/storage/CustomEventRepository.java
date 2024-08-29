package ru.practicum.event.storage;

import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface CustomEventRepository {
    List<Event> findAllEventsByParams(Set<Integer> users,
                                      Set<EventState> states,
                                      Set<Integer> categories,
                                      LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd,
                                      Integer from,
                                      Integer size);

    List<Event> findAllPublishedEventsByParams(String text,
                                               Set<Integer> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd);
}
