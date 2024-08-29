package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.error.exception.ConflictValidationException;
import ru.practicum.error.exception.EntityNotFoundException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.user.dto.NewUserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserRepository;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> findAllUsers(Set<Integer> ids, Integer from, Integer size) {
        Pageable page = getPageable(from, size);

        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(page).getContent();
        } else {
            return userRepository.findAllByIdIn(ids, page).getContent();
        }
    }

    public User createUser(NewUserDto userDto) {
        String email = userDto.getEmail();
        String name = userDto.getName();
        validateUserFieldsFormat(email, name);

        User user = UserMapper.toUser(userDto);

        return userRepository.save(user);
    }

    public void deleteUser(Integer userId) {
        validateUserId(userId);
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    public User findUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("Пользователя с Id = {} не существует", userId);
            throw new EntityNotFoundException("Пользователя с Id = " + userId + " не существует");
        });
    }

    private void validateUserFieldsFormat(String email, String name) {
        if (email == null || name == null) {
            log.warn("Обязательные поля не заполнены");
            throw new ValidationException("Обязательные поля не заполнены");
        }
        if (email.isBlank() || email.indexOf('@') == -1) {
            log.warn("Электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (name.isBlank()) {
            log.warn("Имя не может состоять из пробелов");
            throw new ValidationException("Имя не может состоять из пробелов");
        }

        if (name.length() < 2) {
            log.warn("Имя не может состоять меньше, чем из 2-х символов");
            throw new ValidationException("Имя не может состоять меньше, чем из 2-х символов");
        }

        if (name.length() > 250) {
            log.warn("Имя не может состоять больше, чем из 250-ти символов");
            throw new ValidationException("Имя не может состоять больше, чем из 250-ти символов");
        }

        if (email.length() < 6) {
            log.warn("Электронная почта не может состоять меньше, чем из 6-ти символов");
            throw new ValidationException("Электронная почта не может состоять меньше, чем из 6-ти символов");
        }
        if (email.length() > 254) {
            log.warn("Электронная почта не может состоять больше, чем из 254-х символов");
            throw new ValidationException("Электронная почта не может состоять больше, чем из 254-х символов");
        }

        if (email.indexOf('@') > 63 || email.indexOf('@') == 0) {
            log.warn("Электронная почта указана неверно");
            throw new ValidationException("Электронная почта указана неверно");
        }

        String substrEmail = email.substring(email.indexOf('@'), email.lastIndexOf('.'));
        String domainDot = "";
        if (substrEmail.indexOf('.') == -1) {
            domainDot = substrEmail;
        } else {
            domainDot = substrEmail.substring(substrEmail.lastIndexOf('.'));
        }
        if ((domainDot.length() - 1) > 63) {
            log.warn("Домен электронной почты указан неверно email: {} ; domain: {}", email, email.substring(email.indexOf('.')));
            throw new ValidationException("Домен электронной почты указан неверно");
        }

        User sameUser = userRepository.findByEmail(email);
        if (sameUser != null) {
            log.warn("Пользователь с такой электронной почтой уже есть в системе");
            throw new ConflictValidationException("Пользователь с такой электронной почтой уже есть в системе");
        }
    }

    private void validateUserId(Integer userId) {
        if (userId == null) {
            log.error("Id не заполнен");
            throw new ValidationException("Для удаления данных пользователя надо указать его Id");
        }
    }

    private Pageable getPageable(Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            log.error("Некорректные значения параметров from = {}, size={}", from, size);
            throw new ValidationException("Некорректные значения параметров from/size");
        }

        Sort sortByEnd = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortByEnd);
        return page;
    }
}
