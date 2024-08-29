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
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.*;
import ru.practicum.event.storage.EventRepository;
import ru.practicum.location.model.Location;
import ru.practicum.location.service.LocationService;
import ru.practicum.request.dto.StatusUpdateRequestDto;
import ru.practicum.request.dto.StatusUpdateResultDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.storage.RequestRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.StatsResponseDto;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final LocationService locationService;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    private static void validateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeEnd != null && rangeStart != null && rangeEnd.isBefore(rangeStart)) {
            log.warn("Неверно указан диапазон дат");
            throw new ValidationException("Неверно указан диапазон дат");
        }
    }

    public Event create(NewEventDto newEventDto, Integer userId) {

        validateNewEventDto(newEventDto);

        Category category = categoryService.findCategoryById(newEventDto.getCategory());

        Location location = locationService.findLocationById(newEventDto.getLocation());

        User user = userService.findUserById(userId);

        Event event = EventMapper.toNewEntity(newEventDto, category, location, user);

        return eventRepository.save(event);
    }

    public List<Event> findAllEventsAddedByUser(Integer userId, Integer from, Integer size) {
        userService.findUserById(userId);
        Pageable page = getPageable(from, size);

        return eventRepository.findAllByInitiatorId(userId, page);
    }

    public List<Event> findAllByListEventsIds(List<Integer> eventIds) {
        return eventRepository.findAllById(eventIds);
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
        if (!event.getInitiator().equals(user)) {
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
            Location location = locationService.findLocationById(updateEventUserDto.getLocation());
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

    public Integer getConfirmedRequests(Integer eventId) {
        return requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED).size();
    }

    public Integer getViews(Event event) {
        List<String> uris = new ArrayList<>();
        uris.add("/events/" + event.getId());
        List<StatsResponseDto> stats = statsClient.getStats(event.getCreated(), LocalDateTime.now(), uris, true);
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
        for (Event event : events) {
            uris.add("/events/" + event.getId());
            if (event.getCreated().isBefore(start)) {
                start = event.getCreated();
            }
        }
        List<StatsResponseDto> stats = statsClient.getStats(start, LocalDateTime.now(), uris, false);
        for (StatsResponseDto statsResponseDto : stats) {
            String uri = statsResponseDto.getUri();
            if (!uri.equals("/events")) {
                Integer eventId = Integer.valueOf(uri.substring(uri.lastIndexOf('/') + 1));
                viewsMap.put(eventId, statsResponseDto.getHits());
            }
        }

        return viewsMap;
    }

    public HashMap<Integer, Integer> getConfirmedRequestsList(List<Event> events) {
        return requestRepository.getCountOfEventsRequestsMap(events.stream().map(Event::getId).collect(Collectors.toSet()));
    }

    public List<Request> findAllRequestsOfUserEvent(Integer userId, Integer eventId) {
        if (userId == null || eventId == null) {
            log.warn("Обязательные поля не заполнены");
            throw new ValidationException("Обязательные поля не заполнены");
        }
        userService.findUserById(userId);
        Event event = findEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Пользователь не являяется инициатором события");
            throw new ConflictValidationException("Пользователь не являяется инициатором события");
        }

        return requestRepository.findAllByEventId(eventId);
    }

    public StatusUpdateResultDto changeRequestsStatus(Integer userId, Integer eventId, StatusUpdateRequestDto statusUpdateRequestDto) {
        if (userId == null || eventId == null) {
            log.warn("Обязательные поля не заполнены");
            throw new ValidationException("Обязательные поля не заполнены");
        }
        userService.findUserById(userId);
        Event event = findEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Пользователь не являяется инициатором события");
            throw new ConflictValidationException("Пользователь не являяется инициатором события");
        }

        if ((!event.getRequestModeration() || event.getParticipantLimit() == 0) && statusUpdateRequestDto.getStatus().equals(RequestStatus.CONFIRMED)) {
            log.warn("подтверждение заявок не требуется");
            throw new ValidationException("подтверждение заявок не требуется");
        }

        Integer countOfConfirmedRequests = getConfirmedRequests(eventId);
        if (countOfConfirmedRequests.equals(event.getParticipantLimit()) && event.getParticipantLimit() != 0) {
            log.warn("достигнут лимит по заявкам на данное событие");
            throw new ConflictValidationException("достигнут лимит по заявкам на данное событие");
        }

        if (statusUpdateRequestDto.getStatus().equals(RequestStatus.PENDING)) {
            log.warn("Неверный статус");
            throw new ValidationException("Неверный статус");
        }

        List<Request> requests = requestRepository.findAllByStatusAndIdIn(RequestStatus.PENDING, statusUpdateRequestDto.getRequestIds());

        if (requests.size() != statusUpdateRequestDto.getRequestIds().size()) {
            log.warn("статус можно изменить только у заявок, находящихся в состоянии ожидания");
            throw new ConflictValidationException("статус можно изменить только у заявок, находящихся в состоянии ожидания");
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        StatusUpdateResultDto result = null;

        if (statusUpdateRequestDto.getStatus().equals(RequestStatus.CONFIRMED)) {
            for (Request request : requests) {
                if (countOfConfirmedRequests <= event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(request);
                    countOfConfirmedRequests++;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(request);
                }
                List<Request> savedConfirmedRequests = requestRepository.saveAll(confirmedRequests);
                List<Request> savedRejectedRequests = requestRepository.saveAll(rejectedRequests);
                result = new StatusUpdateResultDto(RequestMapper.toDto(savedConfirmedRequests), RequestMapper.toDto(savedRejectedRequests));
            }
        } else {
            for (Request request : requests) {
                request.setStatus(RequestStatus.REJECTED);
            }
            List<Request> savedRequests = requestRepository.saveAll(requests);
            result = new StatusUpdateResultDto(null, RequestMapper.toDto(savedRequests));
        }

        return result;
    }

    public List<Event> findAllEventsByParams(Set<Integer> users,
                                             Set<EventState> states,
                                             Set<Integer> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             Integer from,
                                             Integer size) {
        validate(from, size);
        validateRange(rangeStart, rangeEnd);
        if (users == null && states == null && categories == null && rangeStart == null && rangeEnd == null) {
            Pageable page = getPageable(from, size);
            return eventRepository.findAll(page).getContent();
        }
        return eventRepository.findAllEventsByParams(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    public List<EventShortDto> findAllPublishedEventsByParams(String text,
                                                              Set<Integer> categories,
                                                              Boolean paid,
                                                              LocalDateTime rangeStart,
                                                              LocalDateTime rangeEnd,
                                                              Boolean onlyAvailable,
                                                              SortParams sort,
                                                              Integer from,
                                                              Integer size) {
        validate(from, size);
        validateRange(rangeStart, rangeEnd);

        log.warn("text = " + text);
        log.warn("categories = " + categories);
        if (text == null && categories == null && paid == null && rangeStart == null && rangeEnd == null && onlyAvailable == null && sort == null) {
            Pageable page = getPageable(from, size);
            List<Event> events = eventRepository.findAllByState(EventState.PUBLISHED, page);

            HashMap<Integer, Integer> viewsMap = getViewsList(events);
            HashMap<Integer, Integer> requestsMap = getConfirmedRequestsList(events);
            return EventMapper.toListOfEventShortDto(events, requestsMap, viewsMap);
        }

        List<Event> allEvents = eventRepository.findAllPublishedEventsByParams(text, categories, paid, rangeStart, rangeEnd);
        HashMap<Integer, Integer> viewsMap = getViewsList(allEvents);
        HashMap<Integer, Integer> requestsMap = getConfirmedRequestsList(allEvents);
        List<EventFullDto> allShortEvents = EventMapper.toListOfEventFullDto(allEvents, requestsMap, viewsMap);
        List<EventFullDto> resultFull = new ArrayList<>();

        if (onlyAvailable != null && onlyAvailable) {
            for (EventFullDto eventFullDto : allShortEvents) {
                if (eventFullDto.getConfirmedRequests() < eventFullDto.getParticipantLimit()) {
                    resultFull.add(eventFullDto);
                }
            }
        } else {
            resultFull = new ArrayList<>(allShortEvents);
        }

        if (sort != null) {
            if (sort.equals(SortParams.EVENT_DATE)) {
                resultFull.sort(Comparator.comparing(EventFullDto::getEventDate));
            }

            if (sort.equals(SortParams.VIEWS)) {
                resultFull.sort(Comparator.comparingInt(EventFullDto::getViews));
            }
        }

        if (resultFull.isEmpty()) {
            return new ArrayList<>();
        }
        return EventMapper.toShortFromFullEvent(resultFull.stream().skip(from).limit(size).collect(Collectors.toList()));
    }

    public Event changeEventsByAdmin(Integer eventId, UpdateEventAdminDto updateEventAdminDto) {
        Event event = findEventById(eventId);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            log.warn("дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            throw new ConflictValidationException("дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
        }
        if (updateEventAdminDto.getStateAction() != null) {
            if (updateEventAdminDto.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT) && !event.getState().equals(EventState.PENDING)) {
                log.warn("событие можно публиковать, только если оно в состоянии ожидания публикации");
                throw new ConflictValidationException("событие можно публиковать, только если оно в состоянии ожидания публикации");
            }
            if (updateEventAdminDto.getStateAction().equals(StateActionAdmin.REJECT_EVENT) && !event.getState().equals(EventState.PENDING)) {
                log.warn("событие можно отклонить, только если оно еще не опубликовано");
                throw new ConflictValidationException("событие можно отклонить, только если оно еще не опубликовано");
            }
        }

        validateUpdateAdminEventDto(updateEventAdminDto);

        if (updateEventAdminDto.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminDto.getAnnotation());
        }

        if (updateEventAdminDto.getCategory() != null) {
            Category category = categoryService.findCategoryById(updateEventAdminDto.getCategory());
            event.setCategory(category);
        }

        if (updateEventAdminDto.getDescription() != null) {
            event.setDescription(updateEventAdminDto.getDescription());
        }

        if (updateEventAdminDto.getEventDate() != null) {
            event.setEventDate(updateEventAdminDto.getEventDate());
        }

        if (updateEventAdminDto.getLocation() != null) {
            Location location = locationService.findLocationById(updateEventAdminDto.getLocation());
            event.setLocation(location);
        }

        if (updateEventAdminDto.getPaid() != null) {
            event.setPaid(updateEventAdminDto.getPaid());
        }

        if (updateEventAdminDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminDto.getParticipantLimit());
        }

        if (updateEventAdminDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminDto.getRequestModeration());
        }

        if (updateEventAdminDto.getStateAction() != null) {
            if (updateEventAdminDto.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
            } else if (updateEventAdminDto.getStateAction().equals(StateActionAdmin.REJECT_EVENT)) {
                event.setState(EventState.CANCELED);
            }
        }

        if (updateEventAdminDto.getTitle() != null) {
            event.setTitle(updateEventAdminDto.getTitle());
        }


        return eventRepository.save(event);
    }

    public Event findPublishedEventById(Integer id) {
        Event event = findEventById(id);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.error("Событие с Id = {} не существует", id);
            throw new EntityNotFoundException("Событие с Id = " + id + " не существует");
        }
        return event;
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

        validateParticipantLimitFormat(newEventDto.getParticipantLimit());
    }

    private void validateEventDateFormat(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
            throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }
    }

    private void validateParticipantLimitFormat(Integer limit) {
        if (limit != null && limit < 0) {
            log.warn("Лимит участников не может быть отрицательным");
            throw new ValidationException("Лимит участников не может быть отрицательным");
        }
    }

    private void validateTitleFormat(String title) {
        if (title != null && title.isBlank()) {
            log.warn("Заголовок не может состоять из пробелов");
            throw new ValidationException("Заголовок не может состоять из пробелов");
        }

        if (title != null && (title.length() > 120 || title.length() < 3)) {
            log.warn("Размерность поля заголовок должна быть в интервале [3,120]");
            throw new ValidationException("Размерность поля заголовок должна быть в интервале [3,120]");
        }
    }


    private void validateDescriptionFormat(String description) {
        if (description != null && description.isBlank()) {
            log.warn("Описание не может состоять из пробелов");
            throw new ValidationException("Описание не может состоять из пробелов");
        }

        if (description != null && (description.length() > 7000 || description.length() < 20)) {
            log.warn("Размерность поля описание должна быть в интервале [20,7000]");
            throw new ValidationException("Размерность поля описание должна быть в интервале [20,7000]");
        }
    }

    private void validateAnnotationFormat(String annotation) {
        if (annotation != null && annotation.isBlank()) {
            log.warn("Аннотация не может состоять из пробелов");
            throw new ValidationException("Аннотация не может состоять из пробелов");
        }

        if (annotation != null && (annotation.length() > 2000 || annotation.length() < 20)) {
            log.warn("Размерность поля аннотация должна быть в интервале [20,2000]");
            throw new ValidationException("Размерность поля аннотация должна быть в интервале [20,2000]");
        }
    }

    private Pageable getPageable(Integer from, Integer size) {
        validate(from, size);

        Sort sortBy = Sort.by(Sort.Direction.ASC, "eventDate");
        return PageRequest.of(from / size, size, sortBy);
    }

    private void validate(Integer from, Integer size) {
        if (from != null && from < 0 || size != null && size <= 0) {
            log.error("Некорректные значения параметров from = {}, size={}", from, size);
            throw new ValidationException("Некорректные значения параметров from/size");
        }
    }

    private void validateEventStateForUpdate(EventState state) {
        if (state != null && !state.equals(EventState.CANCELED) && !state.equals(EventState.PENDING)) {
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

        if (updateEventUserDto.getParticipantLimit() != null) {
            validateParticipantLimitFormat(updateEventUserDto.getParticipantLimit());
        }
    }

    private void validateUpdateAdminEventDto(UpdateEventAdminDto updateEventAdminDto) {
        if (updateEventAdminDto.getAnnotation() != null) {
            validateAnnotationFormat(updateEventAdminDto.getAnnotation());
        }

        if (updateEventAdminDto.getDescription() != null) {
            validateDescriptionFormat(updateEventAdminDto.getDescription());
        }

        if (updateEventAdminDto.getTitle() != null) {
            validateTitleFormat(updateEventAdminDto.getTitle());
        }

        if (updateEventAdminDto.getParticipantLimit() != null) {
            validateParticipantLimitFormat(updateEventAdminDto.getParticipantLimit());
        }

        if (updateEventAdminDto.getEventDate() != null) {
            validateEventDateFormat(updateEventAdminDto.getEventDate());
        }
    }
}
