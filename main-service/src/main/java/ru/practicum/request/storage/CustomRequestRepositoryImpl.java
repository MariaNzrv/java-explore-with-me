package ru.practicum.request.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.RequestStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomRequestRepositoryImpl implements CustomRequestRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public HashMap<Integer, Integer> getCountOfEventsRequestsMap(Set<Integer> eventsId) {

        HashMap<Integer, Integer> requestsMap = new HashMap<>();
        if (eventsId.isEmpty()) {
            return requestsMap;
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("statusConfirmed", RequestStatus.CONFIRMED.name());
        params.put("eventsId", eventsId);

        SqlParameterSource parameters = new MapSqlParameterSource(params);
        String sql = "select event_id, count(id) as hits " +
                "from requests " +
                "where event_id in (:eventsId) and status = :statusConfirmed " +
                "group by event_id ";
        SqlRowSet sqlRowSet = namedParameterJdbcTemplate.queryForRowSet(sql, parameters);


        if (sqlRowSet.first()) {
            do {
                requestsMap.put(sqlRowSet.getInt("event_id"), sqlRowSet.getInt("hits"));
            } while (sqlRowSet.next());
        }

        return requestsMap;
    }

    @Override
    public List<RequestDto> findAllRequestsOfUser(Integer userId) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        SqlParameterSource parameters = new MapSqlParameterSource(params);
        String sql = "select r.* " +
                "from requests r, event e " +
                "where r.requester_id = (:userId) and r.event_id = e.id and e.initiator_id <> (:userId) ";
        return namedParameterJdbcTemplate.query(sql, parameters,
                (rs, rowNum) -> new RequestDto(rs.getTimestamp("created").toLocalDateTime(),
                        rs.getInt("event_id"),
                        rs.getInt("id"),
                        rs.getInt("requester_id"),
                        RequestStatus.valueOf(rs.getString("status")))
        );
    }
}
