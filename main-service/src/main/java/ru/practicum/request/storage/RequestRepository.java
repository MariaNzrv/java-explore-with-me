package ru.practicum.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.user.model.User;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Integer>, CustomRequestRepository {
    List<Request> findAllByEventIdAndStatus(Integer eventId, RequestStatus requestStatus);
    Request findByRequesterIdAndEventId(Integer userId, Integer eventId);
}
