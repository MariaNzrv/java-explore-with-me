package ru.practicum.storage;

import ru.practicum.model.Statistic;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomEndpointHistoryRepository {
    List<Statistic> getStatisticBetweenDates(LocalDateTime start, LocalDateTime end);

    List<Statistic> getStatisticBetweenDatesAndUriIn(LocalDateTime start, LocalDateTime end, List<String> uris);

    List<Statistic> getStatisticBetweenDatesGroupByIp(LocalDateTime start, LocalDateTime end);

    List<Statistic> getStatisticBetweenDatesAndUriInGroupByIp(LocalDateTime start, LocalDateTime end, List<String> uris);
}
