package ru.practicum.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.model.App;
import ru.practicum.model.Statistic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomEndpointHistoryRepositoryImpl implements CustomEndpointHistoryRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final AppRepository appRepository;

    @Override
    public List<Statistic> getStatisticBetweenDates(LocalDateTime start, LocalDateTime end) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);

        SqlParameterSource parameters = new MapSqlParameterSource(params);
        String sql = "select distinct app_id, uri, count(id) as hits "
                + "from endpoint_hist "
                + "where request_timestamp between date(:start) and date(:end) "
                + "group by app_id, uri "
                + "order by hits desc";
        SqlRowSet endpointHistRows = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);

        return fillStatistic(endpointHistRows);
    }

    @Override
    public List<Statistic> getStatisticBetweenDatesAndUriIn(LocalDateTime start, LocalDateTime end, List<String> uris) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);
        params.put("uri", uris);

        SqlParameterSource parameters = new MapSqlParameterSource(params);
        String sql = "select distinct app_id, uri, count(id) as hits "
                + "from endpoint_hist "
                + "where request_timestamp between date(:start) and date(:end) and uri in (:uri) "
                + "group by app_id, uri "
                + "order by hits desc";
        SqlRowSet endpointHistRows = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);

        return fillStatistic(endpointHistRows);
    }

    @Override
    public List<Statistic> getStatisticBetweenDatesGroupByIp(LocalDateTime start, LocalDateTime end) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);

        SqlParameterSource parameters = new MapSqlParameterSource(params);
        String sql = "select app_id, uri, count(uri) as hits from ( "
                + "select distinct app_id, uri, ip "
                + "from endpoint_hist "
                + "where request_timestamp between date(:start) and date(:end)) "
                + "group by app_id, uri "
                + "order by hits desc";
        SqlRowSet endpointHistRows = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);

        return fillStatistic(endpointHistRows);
    }

    @Override
    public List<Statistic> getStatisticBetweenDatesAndUriInGroupByIp(LocalDateTime start, LocalDateTime end, List<String> uris) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);
        params.put("uri", uris);

        SqlParameterSource parameters = new MapSqlParameterSource(params);
        String sql = "select app_id, uri, count(uri) as hits from ( "
                + "select distinct app_id, uri, ip "
                + "from endpoint_hist "
                + "where request_timestamp between date(:start) and date(:end) and uri in (:uri)) "
                + "group by app_id, uri "
                + "order by hits desc";
        SqlRowSet endpointHistRows = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);

        return fillStatistic(endpointHistRows);
    }

    private List<Statistic> fillStatistic(SqlRowSet endpointHistRows) {
        List<Statistic> endpointHist = new ArrayList<>();

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
}
