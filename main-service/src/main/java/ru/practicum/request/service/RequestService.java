package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.error.exception.ConflictValidationException;
import ru.practicum.error.exception.EntityNotFoundException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.storage.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;

    public List<Request> getEventConfirmedRequests(Integer eventId) {
        return requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    public List<Request> getPendingRequestsToChange(Set<Integer> ids) {
        return requestRepository.findAllByStatusAndIdIn(RequestStatus.PENDING, ids);
    }

    public Integer getCountOfEventConfirmedRequests(Integer eventId) {
        return getEventConfirmedRequests(eventId).size();
    }

    public HashMap<Integer, Integer> getListOfCountConfirmedRequests(List<Event> events) {
        Set<Integer> eventsIds = new HashSet<>();
        for (Event event : events) {
            eventsIds.add(event.getId());
        }

        return requestRepository.getCountOfEventsRequestsMap(eventsIds);
    }

    public List<RequestDto> findAll(Integer userId) {
        userService.findUserById(userId);
        return requestRepository.findAllRequestsOfUser(userId);
    }

    public Request create(Integer userId, Integer eventId) {
        if (userId == null || eventId == null) {
            log.warn("Обязательные поля не заполнены");
            throw new ValidationException("Обязательные поля не заполнены");
        }
        User user = userService.findUserById(userId);
        Event event = eventService.findEventById(eventId);

        Request sameRequest = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (sameRequest != null) {
            log.error("нельзя добавить повторный запрос");
            throw new ConflictValidationException("нельзя добавить повторный запрос");
        }

        if (event.getInitiator().equals(user)) {
            log.error("инициатор события не может добавить запрос на участие в своём событии");
            throw new ConflictValidationException("инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.error("нельзя участвовать в неопубликованном событии");
            throw new ConflictValidationException("нельзя участвовать в неопубликованном событии");
        }

        if (event.getParticipantLimit().equals(getCountOfEventConfirmedRequests(eventId)) && event.getParticipantLimit() != 0) {
            log.error(" у события достигнут лимит запросов на участие");
            throw new ConflictValidationException(" у события достигнут лимит запросов на участие");
        }

        Request request = new Request();
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }
        return requestRepository.save(request);
    }

    public Request cancel(Integer userId, Integer requestId) {
        if (userId == null || requestId == null) {
            log.warn("Обязательные поля не заполнены");
            throw new ValidationException("Обязательные поля не заполнены");
        }

        User user = userService.findUserById(userId);
        Request request = findRequestById(requestId);

        if (!request.getRequester().getId().equals(userId)) {
            log.error("нельзя отменить чужой запрос");
            throw new ConflictValidationException("нельзя отменить чужой запрос");
        }

        request.setStatus(RequestStatus.CANCELED);

        return requestRepository.save(request);

    }

    public Request findRequestById(Integer requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> {
            log.error("Запроса с Id = {} не существует", requestId);
            throw new EntityNotFoundException("Запроса с Id = " + requestId + " не существует");
        });
    }

    public List<Request> findAllRequestsOfEvent(Integer eventId) {
        return requestRepository.findAllByEventId(eventId);
    }

    public List<Request> save(List<Request> requests) {
        return requestRepository.saveAll(requests);
    }


}
