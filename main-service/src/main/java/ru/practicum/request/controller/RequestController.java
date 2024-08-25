package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.service.RequestService;
import ru.practicum.user.dto.NewUserDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")
public class RequestController {
    private final RequestService requestService;

    @GetMapping
    public List<RequestDto> findAll(@PathVariable Integer userId) {
        return requestService.findAll(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto create(@PathVariable Integer userId, @RequestParam Integer eventId) {
        Request savedRequest = requestService.create(userId, eventId);
        return RequestMapper.toDto(savedRequest);
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto cancel(@PathVariable Integer userId, @PathVariable Integer requestId) {
        return RequestMapper.toDto(requestService.cancel(userId, requestId));
    }
}
