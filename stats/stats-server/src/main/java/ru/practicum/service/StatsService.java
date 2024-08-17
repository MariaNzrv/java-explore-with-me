package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.practicum.HitRequest;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatisticMapper;
import ru.practicum.model.App;
import ru.practicum.model.EndpointHistory;
import ru.practicum.model.Statistic;
import ru.practicum.storage.AppRepository;
import ru.practicum.storage.EndpointHistoryRepository;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final AppRepository appRepository;
    private final EndpointHistoryRepository endpointHistoryRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void createEndpointHistory(HitRequest hitRequest) {
        validatePostFields(hitRequest);

        App app = getAppByName(hitRequest.getApp());
        EndpointHistory endpointHistory = StatisticMapper.toEndpointHistory(hitRequest);
        endpointHistory.setApp(app);

        endpointHistoryRepository.save(endpointHistory);
    }

    private App getAppByName(String name) {
        Optional<App> app = appRepository.findByName(name);
        return app.orElseGet(() -> appRepository.save(new App(name)));
    }

    private void validatePostFields(HitRequest hitRequest) {
        if (hitRequest.getApp() == null || hitRequest.getIp() == null || hitRequest.getUri() == null ||
                hitRequest.getTimestamp() == null) {
            log.warn("Не заполнены обязательные поля");
            throw new ValidationException("Не заполнены обязательные поля");
        }
    }

    public List<Statistic> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        validateGetFields(start, end);

        List<Statistic> endpointHist = new ArrayList<>();
        String sql;
        SqlRowSet endpointHistRows = null;
        HashMap<String, Object> params = new HashMap<>();

        if (!unique && (uris == null || uris.isEmpty())) {
            params.put("start", start);
            params.put("end", end);
            SqlParameterSource parameters = new MapSqlParameterSource(params);
            sql = "select distinct app_id, uri, count(id) as hits " +
                    "from endpoint_hist " +
                    "where request_timestamp between date(:start) and date(:end) " +
                    "group by app_id, uri " +
                    "order by hits desc";
            endpointHistRows = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);

        }

        if (!unique && uris != null && !uris.isEmpty()){
            params.put("start", start);
            params.put("end", end);
            params.put("uri", uris);
            SqlParameterSource parameters = new MapSqlParameterSource(params);
            sql = "select distinct app_id, uri, count(id) as hits " +
                    "from endpoint_hist " +
                    "where request_timestamp between date(:start) and date(:end) and uri in (:uri) " +
                    "group by app_id, uri " +
                    "order by hits desc";
            endpointHistRows = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);
        }

        if (unique && (uris == null || uris.isEmpty())) {
            params.put("start", start);
            params.put("end", end);
            SqlParameterSource parameters = new MapSqlParameterSource(params);
            sql = "select app_id, uri, count(uri) as hits from ( " +
                    "select distinct app_id, uri, ip " +
                    "from endpoint_hist " +
                    "where request_timestamp between date(:start) and date(:end)) " +
                    "group by app_id, uri " +
                    "order by hits desc";
            endpointHistRows = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);
        }

        if (unique && uris != null && !uris.isEmpty()){
            params.put("start", start);
            params.put("end", end);
            params.put("uri", uris);
            SqlParameterSource parameters = new MapSqlParameterSource(params);
            sql = "select app_id, uri, count(uri) as hits from ( " +
                    "select distinct app_id, uri, ip " +
                    "from endpoint_hist " +
                    "where request_timestamp between date(:start) and date(:end) and uri in (:uri)) " +
                    "group by app_id, uri " +
                    "order by hits desc";
            endpointHistRows = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);
        }

        if (endpointHistRows.first()) {
            Optional<App> app = appRepository.findById(endpointHistRows.getInt("app_id"));
            if (app.isPresent()) {
                endpointHist.add(new Statistic(app.get(), endpointHistRows.getString("uri"), endpointHistRows.getInt("hits")));
            }
            while (endpointHistRows.next()) {
                app = appRepository.findById(endpointHistRows.getInt("app_id"));
                if (app.isPresent()) {
                    endpointHist.add(new Statistic(app.get(), endpointHistRows.getString("uri"), endpointHistRows.getInt("hits")));
                }
            }
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
