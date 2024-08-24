package ru.practicum.request.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.request.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class CustomRequestRepositoryImpl implements CustomRequestRepository{
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public HashMap<Integer, Integer> getCountOfEventsRequestsMap(Set<Integer> eventsId) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("statusConfirmed", RequestStatus.CONFIRMED);
        params.put("eventsId", eventsId);

        SqlParameterSource parameters = new MapSqlParameterSource(params);
        String sql = "select event_id, count(id) as hits " +
                "from requests " +
                "group by event_id " +
                "having event_id in (:eventsId) and status = (:statusConfirmed)";
        SqlRowSet sqlRowSet = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);

        HashMap<Integer, Integer> requestsMap = new HashMap<>();

        if (sqlRowSet.first()) {
            do {
                requestsMap.put(sqlRowSet.getInt("event_id"), sqlRowSet.getInt("hits"));
            } while (sqlRowSet.next());
        }

        return requestsMap;
    }
}
