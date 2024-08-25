package ru.practicum.request.mapper;

import ru.practicum.event.model.Event;
import ru.practicum.request.model.Request;
import ru.practicum.user.dto.NewUserDto;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class RequestMapper {
    public static RequestDto toDto(Request request) {
        return new RequestDto(
                request.getCreated(),
                request.getEvent().getId(),
                request.getId(),
                request.getRequester().getId(),
                request.getStatus());
    }

    public static List<RequestDto> toDto(List<Request> requests) {
        List<RequestDto> requestDtos = new ArrayList<>();
        for (Request request : requests) {
            requestDtos.add(toDto(request));
        }
        return requestDtos;
    }

    public static Request toEntity(RequestDto requestDto, User user, Event event) {
        return new Request(
                requestDto.getId(),
                requestDto.getStatus(),
                user,
                event,
                requestDto.getCreated());
    }
}
