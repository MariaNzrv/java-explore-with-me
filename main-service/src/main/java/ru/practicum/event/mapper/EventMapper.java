package ru.practicum.event.mapper;

import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.location.model.Location;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventMapper {

    public static EventFullDto toDto(Event event, Integer confirmedRequests, Integer views) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(CategoryMapper.toDto(event.getCategory()));
        if (confirmedRequests != null) {
            eventFullDto.setConfirmedRequests(confirmedRequests);
        }
        eventFullDto.setCreatedOn(event.getCreated());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setId(event.getId());
        eventFullDto.setInitiator(UserMapper.toShortDto(event.getInitiator()));
        eventFullDto.setLocation(event.getLocation());
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setPublishedOn(event.getPublishedOn());
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setState(event.getState());
        eventFullDto.setTitle(event.getTitle());
        if (views != null) {
            eventFullDto.setViews(views);
        }
        return eventFullDto;
    }

    public static EventShortDto toShortDto(Event event, Integer confirmedRequests, Integer views) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setCategory(CategoryMapper.toDto(event.getCategory()));
        eventShortDto.setConfirmedRequests(confirmedRequests);
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setId(event.getId());
        eventShortDto.setInitiator(UserMapper.toShortDto(event.getInitiator()));
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setViews(views);
        return eventShortDto;
    }

    public static EventShortDto toShortFromFullEvent(EventFullDto eventFullDto) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setAnnotation(eventFullDto.getAnnotation());
        eventShortDto.setCategory(eventFullDto.getCategory());
        eventShortDto.setConfirmedRequests(eventFullDto.getConfirmedRequests());
        eventShortDto.setEventDate(eventFullDto.getEventDate());
        eventShortDto.setId(eventFullDto.getId());
        eventShortDto.setInitiator(eventFullDto.getInitiator());
        eventShortDto.setPaid(eventFullDto.getPaid());
        eventShortDto.setTitle(eventFullDto.getTitle());
        eventShortDto.setViews(eventFullDto.getViews());
        return eventShortDto;
    }

    public static List<EventShortDto> toShortFromFullEvent(List<EventFullDto> eventFullDtos) {
        List<EventShortDto> eventShortDtos = new ArrayList<>();
        for (EventFullDto eventFullDto : eventFullDtos) {
            eventShortDtos.add(toShortFromFullEvent(eventFullDto));
        }
        return eventShortDtos;
    }


    public static Event toNewEntity(NewEventDto eventDto, Category category, Location location, User user) {
        Event event = new Event();
        event.setAnnotation(eventDto.getAnnotation());
        event.setDescription(eventDto.getDescription());
        event.setEventDate(eventDto.getEventDate());
        event.setCategory(category);
        event.setLocation(location);
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        event.setTitle(eventDto.getTitle());
        event.setInitiator(user);
        event.setState(EventState.PENDING);
        return event;
    }

    public static Event toUpdateEntity(UpdateEventUserDto eventDto, Category category, Location location, User user, EventState state) {
        Event event = new Event();
        event.setAnnotation(eventDto.getAnnotation());
        event.setDescription(eventDto.getDescription());
        event.setEventDate(eventDto.getEventDate());
        event.setCategory(category);
        event.setLocation(location);
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        event.setTitle(eventDto.getTitle());
        event.setInitiator(user);
        event.setState(state);
        return event;
    }

    public static List<EventFullDto> toListOfEventFullDto(List<Event> events, HashMap<Integer, Integer> requestsMap, HashMap<Integer, Integer> viewsMap) {
        List<EventFullDto> eventFullDtos = new ArrayList<>();

        for (Event event: events) {
            eventFullDtos.add(EventMapper.toDto(event, requestsMap.get(event.getId()), viewsMap.get(event.getId())));
        }

        return eventFullDtos;
    }

    public static List<EventShortDto> toListOfEventShortDto(List<Event> events, HashMap<Integer, Integer> requestsMap, HashMap<Integer, Integer> viewsMap) {
        List<EventShortDto> eventShortDtos = new ArrayList<>();

        for (Event event: events) {
            eventShortDtos.add(EventMapper.toShortDto(event, requestsMap.get(event.getId()), viewsMap.get(event.getId())));
        }

        return eventShortDtos;
    }
}
