package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.error.exception.ConflictValidationException;
import ru.practicum.error.exception.EntityNotFoundException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.StateActionUser;
import ru.practicum.location.service.LocationService;
import ru.practicum.request.service.RequestService;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.StatsResponseDto;
import ru.practicum.user.model.User;
import ru.practicum.event.storage.EventRepository;
import ru.practicum.location.model.Location;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final LocationService locationService;
    private final RequestService requestService;
    private final StatsClient statsClient;

    public Event create(NewEventDto newEventDto, Integer userId) {
        
        validateNewEventDto(newEventDto);

        Category category = categoryService.findCategoryById(newEventDto.getCategory());

        Location location  = locationService.findLocationById(newEventDto.getLocation());

        User user = userService.findUserById(userId);

        Event event = EventMapper.toNewEntity(newEventDto, category, location, user);

        return eventRepository.save(event);
    }

    public List<Event> findAllEventsAddedByUser(Integer userId, Integer from, Integer size) {
        userService.findUserById(userId);
        Pageable page = getPageable(from, size);

        return eventRepository.findAllByInitiatorId(userId, page);
    }

    public Event findUserEventById(Integer eventId, Integer userId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            log.error("Событие с Id = {} не существует", eventId);
            throw new EntityNotFoundException("Событие с Id = " + eventId + " не существует");
        }
        return event;
    }

    public Event updateEventByUser(Integer userId, Integer eventId, UpdateEventUserDto updateEventUserDto) {
        Event event = findEventById(eventId);
        EventState state = event.getState();
        User user = userService.findUserById(userId);
        if(!event.getInitiator().equals(user)) {
            log.warn("Событие недоступно для редактирования");
            throw new EntityNotFoundException("Событие недоступно для редактирования");
        }

        validateEventStateForUpdate(state);

        validateUpdateEventDto(updateEventUserDto);

        if (updateEventUserDto.getAnnotation() != null) {
            event.setAnnotation(updateEventUserDto.getAnnotation());
        }

        if (updateEventUserDto.getCategory() != null) {
            Category category = categoryService.findCategoryById(updateEventUserDto.getCategory());
            event.setCategory(category);
        }

        if (updateEventUserDto.getDescription() != null) {
            event.setDescription(updateEventUserDto.getDescription());
        }

        if (updateEventUserDto.getEventDate() != null) {
            event.setEventDate(updateEventUserDto.getEventDate());
        }

        if (updateEventUserDto.getLocation() != null) {
            Location location  = locationService.findLocationById(updateEventUserDto.getLocation());
            event.setLocation(location);
        }

        if (updateEventUserDto.getPaid() != null) {
            event.setPaid(updateEventUserDto.getPaid());
        }

        if (updateEventUserDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserDto.getParticipantLimit());
        }

        if (updateEventUserDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserDto.getRequestModeration());
        }

        if (updateEventUserDto.getStateAction() != null) {
            if (updateEventUserDto.getStateAction().equals(StateActionUser.SEND_TO_REVIEW)) {
                state = EventState.PENDING;
            } else if (updateEventUserDto.getStateAction().equals(StateActionUser.CANCEL_REVIEW)) {
                state = EventState.CANCELED;
            }
            event.setState(state);
        }

        if (updateEventUserDto.getTitle() != null) {
            event.setTitle(updateEventUserDto.getTitle());
        }

        return eventRepository.save(event);
    }

    public Event findEventById(Integer eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> {
            log.error("Событие с Id = {} не существует", eventId);
            throw new EntityNotFoundException("Событие с Id = " + eventId + " не существует");
        });
    }

    public Integer getConfirmedRequests (Integer eventId) {
        return requestService.getCountOfEventConfirmedRequests(eventId);
    }

    public Integer getViews (Event event) {
        List<String> uris = new ArrayList<>();
        uris.add("http://localhost:8080/events/" + event.getId());
        List<StatsResponseDto> stats = statsClient.getStats(event.getCreated(), LocalDateTime.now(), uris, false);
        if (stats != null && !stats.isEmpty()) {
            return stats.get(0).getHits();
        } else {
            return 0;
        }
    }

    public HashMap<Integer, Integer> getViewsList(List<Event> events) {
        HashMap<Integer, Integer> viewsMap = new HashMap<>();
        List<String> uris = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now();
        for (Event event: events) {
            uris.add("http://localhost:8080/events/" + event.getId());
            if (event.getCreated().isBefore(start)) {
                start = event.getCreated();
            }
        }
        List<StatsResponseDto> stats = statsClient.getStats(start, LocalDateTime.now(), uris, false);
        for (StatsResponseDto statsResponseDto: stats) {
            String uri = statsResponseDto.getUri();
            Integer eventId = Integer.valueOf(uri.substring(uri.lastIndexOf('/')));
            viewsMap.put(eventId, statsResponseDto.getHits());
        }

        return viewsMap;
    }

    public HashMap<Integer, Integer> getConfirmedRequestsList(List<Event> events) {
       return requestService.getListOfCountConfirmedRequests(events);
    }

    private void validateNewEventDto(NewEventDto newEventDto) {
        
        if (newEventDto.getAnnotation() == null ||
                newEventDto.getCategory() == null ||
                newEventDto.getDescription() == null ||
                newEventDto.getEventDate() == null ||
                newEventDto.getLocation() == null ||
                newEventDto.getTitle() == null) {
            log.warn("Обязательные поля не заполнены");
            throw new ValidationException("Обязательные поля не заполнены");
        }

        validateAnnotationFormat(newEventDto.getAnnotation());

        validateDescriptionFormat(newEventDto.getDescription());

        validateTitleFormat(newEventDto.getTitle());

        validateEventDateFormat(newEventDto.getEventDate());
    }

    private void validateEventDateFormat(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
            throw new ConflictValidationException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }
    }

    private void validateTitleFormat(String title) {
        if (title.isBlank()) {
            log.warn("Заголовок не может состоять из пробелов");
            throw new ValidationException("Заголовок не может состоять из пробелов");
        }

        if (title.length() > 120 || title.length() < 3) {
                log.warn("Размерность поля заголовок должна быть в интервале [3,120]");
                throw new ValidationException("Размерность поля заголовок должна быть в интервале [3,120]");
        }
    }


    private void validateDescriptionFormat(String description) {
        if (description.isBlank()) {
            log.warn("Описание не может состоять из пробелов");
            throw new ValidationException("Описание не может состоять из пробелов");
        }

        if (description.length() > 7000 || description.length() < 20) {
            log.warn("Размерность поля описание должна быть в интервале [20,7000]");
            throw new ValidationException("Размерность поля описание должна быть в интервале [20,7000]");
        }
    }

    private void validateAnnotationFormat(String annotation) {
        if (annotation.isBlank()) {
            log.warn("Аннотация не может состоять из пробелов");
            throw new ValidationException("Аннотация не может состоять из пробелов");
        }

        if (annotation.length() > 2000 || annotation.length() < 20) {
            log.warn("Размерность поля аннотация должна быть в интервале [20,2000]");
            throw new ValidationException("Размерность поля аннотация должна быть в интервале [20,2000]");
        }
    }

    private Pageable getPageable(Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            log.error("Некорректные значения параметров from = {}, size={}", from, size);
            throw new ValidationException("Некорректные значения параметров from/size");
        }

        Sort sortBy = Sort.by(Sort.Direction.ASC, "event_date");
        return PageRequest.of(from / size, size, sortBy);
    }

    private void validateEventStateForUpdate(EventState state) {
        if (!state.equals(EventState.CANCELED) && !state.equals(EventState.PENDING)) {
            log.error("Событие не удовлетворяет правилам редактирования");
            throw new ConflictValidationException("Событие не удовлетворяет правилам редактирования");
        }

    }

    private void validateUpdateEventDto(UpdateEventUserDto updateEventUserDto) {
        if (updateEventUserDto.getAnnotation() != null) {
            validateAnnotationFormat(updateEventUserDto.getAnnotation());
        }

        if (updateEventUserDto.getDescription() != null) {
            validateDescriptionFormat(updateEventUserDto.getDescription());
        }

        if (updateEventUserDto.getEventDate() != null) {
            validateEventDateFormat(updateEventUserDto.getEventDate());
        }

        if (updateEventUserDto.getTitle() != null) {
            validateTitleFormat(updateEventUserDto.getTitle());
        }
    }
}
