package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.UpdateEventAdminDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> findAllEventsByParams( @RequestParam(required = false, name = "users") Set<Integer> users,
                                                        @RequestParam(required = false, name = "states") Set<EventState> states,
                                                        @RequestParam(required = false, name = "categories") Set<Integer> categories,
                                                        @RequestParam(required = false, name = "rangeStart") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                        @RequestParam(required = false, name = "rangeEnd") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                        @RequestParam(defaultValue = "0", required = false, name = "from") Integer from,
                                                        @RequestParam(defaultValue = "10", required = false, name = "size") Integer size) {
        List<Event> events = eventService.findAllEventsByParams(users, states, categories, rangeStart, rangeEnd, from, size);
        HashMap<Integer, Integer> viewsMap = eventService.getViewsList(events);
        HashMap<Integer, Integer> requestsMap = eventService.getConfirmedRequestsList(events);
        return EventMapper.toListOfEventFullDto(events, requestsMap, viewsMap);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto changeEventsByAdmin(@PathVariable Integer eventId, @RequestBody UpdateEventAdminDto updateEventAdminDto) {
        Event event = eventService.changeEventsByAdmin(eventId, updateEventAdminDto);
        Integer confirmedRequests = eventService.getConfirmedRequests(eventId);
        Integer views = eventService.getViews(event);
        return EventMapper.toDto(event, confirmedRequests, views);
    }
}
