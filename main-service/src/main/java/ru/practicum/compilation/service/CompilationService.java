package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.storage.CompilationRepository;
import ru.practicum.error.exception.EntityNotFoundException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationService {
    private final EventService eventService;
    private final CompilationRepository compilationRepository;

    public List<CompilationDto> findAllCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations = new ArrayList<>();
        Pageable page = getPageable(from, size);
        if (pinned == null) {
            compilations = compilationRepository.findAll(page).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, page);
        }
        List<CompilationDto> result = new ArrayList<>();
        for (Compilation compilation : compilations) {
            List<Event> eventList = compilation.getEvents().stream().toList();
            HashMap<Integer, Integer> views = eventService.getViewsList(eventList);
            HashMap<Integer, Integer> requests = eventService.getConfirmedRequestsList(eventList);
            List<EventShortDto> eventShortDtos = EventMapper.toListOfEventShortDto(eventList, requests, views);
            result.add(CompilationMapper.toDto(compilation, eventShortDtos));
        }

        return result;
    }

    public CompilationDto findCompilationById(Integer compId) {
        Compilation compilation = findById(compId);
        List<Event> eventList = compilation.getEvents().stream().toList();
        HashMap<Integer, Integer> views = eventService.getViewsList(eventList);
        HashMap<Integer, Integer> requests = eventService.getConfirmedRequestsList(eventList);
        List<EventShortDto> eventShortDtos = EventMapper.toListOfEventShortDto(eventList, requests, views);
        return CompilationMapper.toDto(compilation, eventShortDtos);
    }

    public Compilation findById(Integer compId) {
        return compilationRepository.findById(compId).orElseThrow(() -> {
            log.error("Подборка с Id = {} не существует", compId);
            throw new EntityNotFoundException("Подборка с Id = " + compId + " не существует");
        });
    }

    public CompilationDto create(NewCompilationDto newCompilationDto) {
        String title = newCompilationDto.getTitle();
        validateTitle(title);
        Compilation compilation = CompilationMapper.toEntity(newCompilationDto);
        HashMap<Integer, Integer> views = new HashMap<>();
        HashMap<Integer, Integer> requests = new HashMap<>();
        List<EventShortDto> eventShortDtos = new ArrayList<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventService.findAllByListEventsIds(newCompilationDto.getEvents().stream().toList());
            compilation.setEvents(new HashSet<>(events));
            views = eventService.getViewsList(events);
            requests = eventService.getConfirmedRequestsList(events);
            eventShortDtos = EventMapper.toListOfEventShortDto(events, requests, views);
        }
        Compilation savedCompilation = compilationRepository.save(compilation);

        return CompilationMapper.toDto(savedCompilation, eventShortDtos);
    }

    public void delete(Integer compId) {
        Compilation compilation = findById(compId);
        compilationRepository.delete(compilation);
    }

    public CompilationDto update(Integer compId, UpdateCompilationDto updateCompilationDto) {
        Compilation compilation = findById(compId);
        HashMap<Integer, Integer> views = new HashMap<>();
        HashMap<Integer, Integer> requests = new HashMap<>();
        List<EventShortDto> eventShortDtos = new ArrayList<>();

        if (updateCompilationDto.getTitle() != null) {
            validateTitle(updateCompilationDto.getTitle());
            compilation.setTitle(updateCompilationDto.getTitle());
        }

        if (updateCompilationDto.getEvents() != null && !updateCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventService.findAllByListEventsIds(updateCompilationDto.getEvents().stream().toList());
            compilation.setEvents(new HashSet<>(events));
            views = eventService.getViewsList(events);
            requests = eventService.getConfirmedRequestsList(events);
            eventShortDtos = EventMapper.toListOfEventShortDto(events, requests, views);
        }

        if (updateCompilationDto.getPinned() != null) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toDto(savedCompilation, eventShortDtos);
    }

    private Pageable getPageable(Integer from, Integer size) {
        validateFromSize(from, size);

        Sort sortBy = Sort.by(Sort.Direction.ASC, "id");
        return PageRequest.of(from / size, size, sortBy);
    }

    private void validateFromSize(Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            log.error("Некорректные значения параметров from = {}, size={}", from, size);
            throw new ValidationException("Некорректные значения параметров from/size");
        }
    }

    private void validateTitle(String title) {
        if (title == null) {
            log.warn("Обязательные поля не заполнены");
            throw new ValidationException("Обязательные поля не заполнены");
        }
        if (title.isBlank()) {
            log.warn("Заголовок не может состоять из пробелов");
            throw new ValidationException("Заголовок не может состоять из пробелов");
        }

        if (title.length() > 50 || title.isEmpty()) {
            log.warn("Размерность поля заголовок должна быть в интервале [1,50]");
            throw new ValidationException("Размерность поля заголовок должна быть в интервале [1,50]");
        }
    }
}
