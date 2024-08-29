package ru.practicum.request.storage;

import ru.practicum.request.dto.RequestDto;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface CustomRequestRepository {
    HashMap<Integer, Integer> getCountOfEventsRequestsMap(Set<Integer> eventsId);

    List<RequestDto> findAllRequestsOfUser(Integer userId);
}
