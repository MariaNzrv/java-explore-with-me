package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mapper.StatisticMapper;
import ru.practicum.service.StatsService;
import ru.practicum.stats.dto.HitRequestDto;
import ru.practicum.stats.dto.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody HitRequestDto hitRequestDto) {
        statsService.createEndpointHistory(hitRequestDto);
    }

    @GetMapping("/stats")
    public List<StatsResponseDto> getStats(@RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                           @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                           @RequestParam(name = "uris", required = false) List<String> uris,
                                           @RequestParam(defaultValue = "false", required = false, name = "unique") Boolean unique) {

        return StatisticMapper.toDto(statsService.getStatistic(start, end, uris, unique));
    }

}
