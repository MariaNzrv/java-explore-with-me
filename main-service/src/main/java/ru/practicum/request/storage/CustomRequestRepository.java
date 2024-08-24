package ru.practicum.request.storage;

import java.util.HashMap;
import java.util.Set;

public interface CustomRequestRepository {
    HashMap<Integer, Integer> getCountOfEventsRequestsMap(Set<Integer> eventsId);
}
