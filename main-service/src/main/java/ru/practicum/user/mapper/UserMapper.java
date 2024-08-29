package ru.practicum.user.mapper;

import ru.practicum.user.dto.NewUserDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {
    public static UserDto toDto(User user) {
        return new UserDto(
                user.getEmail(),
                user.getId(),
                user.getName()
        );
    }

    public static UserShortDto toShortDto(User user) {
        return new UserShortDto(
                user.getId(),
                user.getName()
        );
    }

    public static List<UserDto> toDto(List<User> users) {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(toDto(user));
        }
        return userDtos;
    }

    public static User toUser(NewUserDto userDto) {
        return new User(
                userDto.getEmail(),
                userDto.getName()
        );
    }
}
