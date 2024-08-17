package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.EndpointHistory;

import java.util.List;

public interface EndpointHistoryRepository extends JpaRepository<EndpointHistory, Integer> {
    List<EndpointHistory> findAllByAppId(Integer appId);
}
