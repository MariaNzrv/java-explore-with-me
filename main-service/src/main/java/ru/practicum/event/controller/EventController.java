package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.SortParams;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class EventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> findAllPublishedEventsByParams(@RequestParam(required = false, name = "text") String text,
                                                              @RequestParam(required = false, name = "categories") Set<Integer> categories,
                                                              @RequestParam(required = false, name = "paid") Boolean paid,
                                                              @RequestParam(required = false, name = "rangeStart") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                              @RequestParam(required = false, name = "rangeEnd") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                              @RequestParam(defaultValue = "false", required = false, name = "onlyAvailable") Boolean onlyAvailable,
                                                              @RequestParam(required = false, name = "sort") SortParams sort,
                                                              @RequestParam(defaultValue = "0", required = false, name = "from") Integer from,
                                                              @RequestParam(defaultValue = "10", required = false, name = "size") Integer size) {

        return eventService.findAllPublishedEventsByParams(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }
}
