package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatisticMapper;
import ru.practicum.model.App;
import ru.practicum.model.EndpointHistory;
import ru.practicum.model.Statistic;
import ru.practicum.stats.dto.HitRequestDto;
import ru.practicum.storage.AppRepository;
import ru.practicum.storage.EndpointHistoryRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final AppRepository appRepository;
    private final EndpointHistoryRepository endpointHistoryRepository;

    public void createEndpointHistory(HitRequestDto hitRequestDto) {
        validatePostFields(hitRequestDto);

        App app = getAppByName(hitRequestDto.getApp());
        EndpointHistory endpointHistory = StatisticMapper.toEndpointHistory(hitRequestDto);
        endpointHistory.setApp(app);

        endpointHistoryRepository.save(endpointHistory);
    }

    private App getAppByName(String name) {
        Optional<App> app = appRepository.findByName(name);
        return app.orElseGet(() -> appRepository.save(new App(name)));
    }

    private void validatePostFields(HitRequestDto hitRequestDto) {
        if (hitRequestDto.getApp() == null || hitRequestDto.getIp() == null || hitRequestDto.getUri() == null || hitRequestDto.getTimestamp() == null) {
            log.warn("Не заполнены обязательные поля");
            throw new ValidationException("Не заполнены обязательные поля");
        }
    }

    public List<Statistic> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        validateGetFields(start, end);

        List<Statistic> endpointHist = new ArrayList<>();

        if (!unique && (uris == null || uris.isEmpty())) {
            endpointHist = endpointHistoryRepository.getStatisticBetweenDates(start, end);
        }

        if (!unique && uris != null && !uris.isEmpty()) {
            endpointHist = endpointHistoryRepository.getStatisticBetweenDatesAndUriIn(start, end, uris);
        }

        if (unique && (uris == null || uris.isEmpty())) {
            endpointHist = endpointHistoryRepository.getStatisticBetweenDatesGroupByIp(start, end);
        }

        if (unique && uris != null && !uris.isEmpty()) {
            endpointHist = endpointHistoryRepository.getStatisticBetweenDatesAndUriInGroupByIp(start, end, uris);
        }

        return endpointHist;
    }

    private void validateGetFields(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            log.warn("Не заполнены обязательные поля");
            throw new ValidationException("Не заполнены обязательные поля");
        }

        if (end.isBefore(start)) {
            log.warn("Дата и время конца диапозона (end) не может превышать значение начала диапазона (start)");
            throw new ValidationException("Дата и время конца диапозона (end) не может превышать значение начала диапазона (start)");
        }
    }
}
