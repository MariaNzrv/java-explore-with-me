package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.SortParams;
import ru.practicum.event.service.EventService;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.HitRequestDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class EventController {
    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> findAllPublishedEventsByParams(@RequestParam(required = false, name = "text") String text,
                                                              @RequestParam(required = false, name = "categories") Set<Integer> categories,
                                                              @RequestParam(required = false, name = "paid") Boolean paid,
                                                              @RequestParam(required = false, name = "rangeStart") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                              @RequestParam(required = false, name = "rangeEnd") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                              @RequestParam(defaultValue = "false", required = false, name = "onlyAvailable") Boolean onlyAvailable,
                                                              @RequestParam(required = false, name = "sort") SortParams sort,
                                                              @RequestParam(defaultValue = "0", required = false, name = "from") Integer from,
                                                              @RequestParam(defaultValue = "10", required = false, name = "size") Integer size,
                                                              HttpServletRequest request) {

        sendStat(request);
        return eventService.findAllPublishedEventsByParams(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventFullDto findPublishedEventById(@PathVariable Integer id, HttpServletRequest request) {
        sendStat(request);

        Event savedEvent = eventService.findPublishedEventById(id);
        Integer confirmedRequests = eventService.getConfirmedRequests(id);
        Integer views = eventService.getViews(savedEvent);
        return EventMapper.toDto(savedEvent, confirmedRequests, views);
    }

    private void sendStat(HttpServletRequest request) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        HitRequestDto hitRequestDto = new HitRequestDto("ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(dateTimeFormatter));
        statsClient.postHit(hitRequestDto);
    }
}
