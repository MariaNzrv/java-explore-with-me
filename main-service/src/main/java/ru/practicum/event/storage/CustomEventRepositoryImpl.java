package ru.practicum.event.storage;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomEventRepositoryImpl implements CustomEventRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<Event> findAllEventsByParams(Set<Integer> users,
                                             Set<EventState> states,
                                             Set<Integer> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             Integer from,
                                             Integer size) {


        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        List<Predicate> predicateList = new ArrayList<>();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> eventRoot = query.from(Event.class);

        if (users != null) {
            Path<Object> userIdPath = eventRoot.join("initiator").get("id");
            predicateList.add(userIdPath.in(users));
        }
        if (states != null) {
            predicateList.add(eventRoot.get("state").in(states));
        }
        if (categories != null) {
            Path<Object> categoryIdPath = eventRoot.join("category").get("id");
            predicateList.add(categoryIdPath.in(categories));
        }
        if (rangeStart != null) {
            predicateList.add(cb.greaterThanOrEqualTo(eventRoot.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            predicateList.add(cb.lessThanOrEqualTo(eventRoot.get("eventDate"), rangeEnd));
        }

        query.select(eventRoot).where(predicateList.toArray(new Predicate[0]));
        return entityManager.createQuery(query).setFirstResult(from).setMaxResults(size).getResultList();

    }

    @Override
    public List<Event> findAllPublishedEventsByParams(String text,
                                                      Set<Integer> categories,
                                                      Boolean paid,
                                                      LocalDateTime rangeStart,
                                                      LocalDateTime rangeEnd) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        List<Predicate> predicateList = new ArrayList<>();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> eventRoot = query.from(Event.class);

        predicateList.add(cb.equal(eventRoot.get("state"), EventState.PUBLISHED));

        if (text != null) {
            predicateList.add(
                    cb.or(
                            cb.like(cb.upper(eventRoot.get("annotation")), "%" + text.toUpperCase() + "%"),
                            cb.like(cb.upper(eventRoot.get("description")), "%" + text.toUpperCase() + "%")
                    )
            );
        }
        if (paid != null) {
            if (paid) {
                predicateList.add(cb.isTrue(eventRoot.get("paid")));
            } else {
                predicateList.add(cb.isFalse(eventRoot.get("paid")));
            }
        }
        if (categories != null) {
            Path<Object> categoryIdPath = eventRoot.join("category").get("id");
            predicateList.add(categoryIdPath.in(categories));
        }
        if (rangeStart == null && rangeEnd == null) {
            predicateList.add(cb.greaterThanOrEqualTo(eventRoot.get("eventDate"), LocalDateTime.now()));
        }
        if (rangeStart != null) {
            predicateList.add(cb.greaterThanOrEqualTo(eventRoot.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            predicateList.add(cb.lessThanOrEqualTo(eventRoot.get("eventDate"), rangeEnd));
        }

        query.select(eventRoot).where(predicateList.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }
}
