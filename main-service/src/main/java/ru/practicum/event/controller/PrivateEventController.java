package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.UpdateEventUserDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.StatusUpdateRequestDto;
import ru.practicum.request.dto.StatusUpdateResultDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.service.RequestService;

import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events", consumes = {"*/*"})
public class PrivateEventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Integer userId, @RequestBody NewEventDto newEventDto) {
        Event savedEvent = eventService.create(newEventDto, userId);
        return EventMapper.toDto(savedEvent, null, null);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findAllEventsAddedByUser(@PathVariable Integer userId,
                                       @RequestParam(defaultValue = "0", required = false, name = "from") Integer from,
                                       @RequestParam(defaultValue = "10", required = false, name = "size") Integer size) {
        List<Event> events = eventService.findAllEventsAddedByUser(userId, from, size);
        HashMap<Integer, Integer> viewsMap = eventService.getViewsList(events);
        HashMap<Integer, Integer> requestsMap = eventService.getConfirmedRequestsList(events);
        return EventMapper.toListOfEventShortDto(events, requestsMap, viewsMap);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findAllEventsAddedByUser(@PathVariable Integer userId,
                                                 @PathVariable Integer eventId) {
        Event event = eventService.findUserEventById(eventId, userId);
        Integer confirmedRequests = eventService.getConfirmedRequests(eventId);
        Integer views = eventService.getViews(event);
        return EventMapper.toDto(event, confirmedRequests, views);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findAllEventsAddedByUser(@PathVariable Integer userId,
                                                 @PathVariable Integer eventId,
                                                 @RequestBody UpdateEventUserDto updateEventUserDto) {
        Event savedEvent = eventService.updateEventByUser(userId, eventId, updateEventUserDto);
        Integer confirmedRequests = eventService.getConfirmedRequests(eventId);
        Integer views = eventService.getViews(savedEvent);
        return EventMapper.toDto(savedEvent, confirmedRequests, views);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> findAllRequestsOfUserEvent(@PathVariable Integer userId,
                                                     @PathVariable Integer eventId) {

        return RequestMapper.toDto(eventService.findAllRequestsOfUserEvent(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public StatusUpdateResultDto changeRequestsStatus(@PathVariable Integer userId,
                                                      @PathVariable Integer eventId,
                                                      @RequestBody StatusUpdateRequestDto statusUpdateRequestDto) {

        return eventService.changeRequestsStatus(userId, eventId, statusUpdateRequestDto);
    }



}
