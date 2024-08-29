package ru.practicum.compilation.mapper;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompilationMapper {

    public static CompilationDto toDto(Compilation compilation, List<EventShortDto> eventShortDtos) {
        return new CompilationDto(
                eventShortDtos,
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle()
        );
    }

    public static Compilation toEntity(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        if (newCompilationDto.getPinned() == null) {
            compilation.setPinned(Boolean.FALSE);
        } else {
            compilation.setPinned(newCompilationDto.getPinned());
        }
        Set<Event> events = new HashSet<>();
        return compilation;
    }
}
