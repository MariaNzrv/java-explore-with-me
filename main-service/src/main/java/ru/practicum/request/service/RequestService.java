package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.storage.RequestRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;

    public List<Request> getEventConfirmedRequests(Integer eventId) {
        return requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    public Integer getCountOfEventConfirmedRequests(Integer eventId) {
        return getEventConfirmedRequests(eventId).size();
    }

    public HashMap<Integer, Integer> getListOfCountRequests(List<Event> events) {
        Set<Integer> eventsIds = new HashSet<>();
        for (Event event: events) {
            eventsIds.add(event.getId());
        }

        return requestRepository.getCountOfEventsRequestsMap(eventsIds);
    }
}
