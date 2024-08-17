package ru.practicum.mapper;

import ru.practicum.HitRequest;
import ru.practicum.StatsResponse;
import ru.practicum.model.EndpointHistory;
import ru.practicum.model.Statistic;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StatisticMapper {
    public static EndpointHistory toEndpointHistory(HitRequest hitRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new EndpointHistory(
                hitRequest.getUri(),
                hitRequest.getIp(),
                LocalDateTime.parse(hitRequest.getTimestamp(), formatter));
    }

    public static StatsResponse toDto(Statistic statistic) {
        StatsResponse statsResponse = new StatsResponse();
        statsResponse.setUri(statistic.getUri());
        statsResponse.setHits(statistic.getHits());
        if (statistic.getApp() != null) {
            statsResponse.setApp(statistic.getApp().getName());
        }
        return statsResponse;
    }

    public static List<StatsResponse> toDto(List<Statistic> statisticList) {
        List<StatsResponse> statsResponseList = new ArrayList<>();
        for (Statistic statistic : statisticList) {
            statsResponseList.add(toDto(statistic));
        }
        return statsResponseList;
    }
}
