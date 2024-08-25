package ru.practicum.compilation.mapper;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.model.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompilationMapper {
//    private final EventService eventService;

//    public static CompilationDto toDto(Compilation compilation) {
//        return new CompilationDto(
//                EventMapper.toDto(compilation.getEvents()),
//                compilation.getId(),
//                compilation.getPinned(),
//                compilation.getTitle()
//        );
//    }

//    public static List<CompilationDto> toDto(List<Compilation> compilations) {
//        List<CompilationDto> compilationDtos = new ArrayList<>();
//        for (Compilation compilation : compilations) {
//            compilationDtos.add(toDto(compilation));
//        }
//        return compilationDtos;
//    }

//    public static Compilation toEntity(NewCompilationDto newCompilationDto) {
//        Compilation compilation = new Compilation();
//        compilation.setTitle(newCompilationDto.getTitle());
//        compilation.setPinned(newCompilationDto.getPinned());
//        Set<Event> events = new HashSet<>();
//        for (Integer eventId: newCompilationDto.getEvents()) {
//            events.add(eventService.findById(eventId));
//        }
//        compilation.setEvents(events);
//        return compilation;
//    }
}
