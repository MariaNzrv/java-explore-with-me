package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.HitRequest;
import ru.practicum.StatsResponse;
import ru.practicum.mapper.StatisticMapper;
import ru.practicum.service.StatsService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public void create(@RequestBody HitRequest hitRequest) {
        statsService.createEndpointHistory(hitRequest);
    }

    @GetMapping("/stats")
    public List<StatsResponse> getStats(@RequestParam("start") String start,
                                        @RequestParam("end") String end,
                                        @RequestParam(name = "uris", required = false) List<String> uris,
                                        @RequestParam(defaultValue = "false", required = false, name = "unique") Boolean unique) {

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime startDateTime = LocalDateTime.parse(URLDecoder.decode(start, StandardCharsets.UTF_8), formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(URLDecoder.decode(end, StandardCharsets.UTF_8), formatter);

        return StatisticMapper.toDto(statsService.getStatistic(startDateTime, endDateTime, uris, unique));
    }


}
