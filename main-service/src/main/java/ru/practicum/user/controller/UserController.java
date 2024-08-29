package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> findAll(@RequestParam(required = false, name = "ids") Set<Integer> ids,
                                 @RequestParam(defaultValue = "0", required = false, name = "from") Integer from,
                                 @RequestParam(defaultValue = "10", required = false, name = "size") Integer size) {
        return UserMapper.toDto(userService.findAllUsers(ids, from, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody NewUserDto userDto) {
        User savedUser = userService.createUser(userDto);
        return UserMapper.toDto(savedUser);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }
}
