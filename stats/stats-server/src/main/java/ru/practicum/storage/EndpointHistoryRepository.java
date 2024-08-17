package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.EndpointHistory;
import ru.practicum.model.Statistic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EndpointHistoryRepository extends JpaRepository<EndpointHistory, Integer> {
    List<EndpointHistory> findAllByAppId(Integer appId);
}
