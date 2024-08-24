package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
public class AdminEventController {
    private final EventService eventService;

    private List<EventFullDto> getListOfEventFullDto(List<Event> events) {
        HashMap<Integer, Integer> viewsMap = eventService.getViewsList(events);
        HashMap<Integer, Integer> requestsMap = eventService.getConfirmedRequestsList(events);
        List<EventFullDto> eventFullDtos = new ArrayList<>();

        for (Event event: events) {
            eventFullDtos.add(EventMapper.toDto(event, requestsMap.get(event.getId()), viewsMap.get(event.getId())));
        }

        return eventFullDtos;
    }
}
