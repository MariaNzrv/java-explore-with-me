package ru.practicum.mapper;

import ru.practicum.stats.dto.HitRequestDto;
import ru.practicum.stats.dto.StatsResponseDto;
import ru.practicum.model.EndpointHistory;
import ru.practicum.model.Statistic;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StatisticMapper {
    public static EndpointHistory toEndpointHistory(HitRequestDto hitRequestDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new EndpointHistory(
                hitRequestDto.getUri(),
                hitRequestDto.getIp(),
                LocalDateTime.parse(hitRequestDto.getTimestamp(), formatter));
    }

    public static StatsResponseDto toDto(Statistic statistic) {
        StatsResponseDto statsResponseDto = new StatsResponseDto();
        statsResponseDto.setUri(statistic.getUri());
        statsResponseDto.setHits(statistic.getHits());
        if (statistic.getApp() != null) {
            statsResponseDto.setApp(statistic.getApp().getName());
        }
        return statsResponseDto;
    }

    public static List<StatsResponseDto> toDto(List<Statistic> statisticList) {
        List<StatsResponseDto> statsResponseDtoList = new ArrayList<>();
        for (Statistic statistic : statisticList) {
            statsResponseDtoList.add(toDto(statistic));
        }
        return statsResponseDtoList;
    }
}
