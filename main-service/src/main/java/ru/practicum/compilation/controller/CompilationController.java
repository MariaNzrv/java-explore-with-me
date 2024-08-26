package ru.practicum.compilation.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.SortParams;
import ru.practicum.event.service.EventService;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/compilations")
public class CompilationController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> findAllCompilations(@RequestParam(required = false, name = "pinned") Boolean pinned,
                                                               @RequestParam(defaultValue = "0", required = false, name = "from") Integer from,
                                                               @RequestParam(defaultValue = "10", required = false, name = "size") Integer size) {

        return compilationService.findAllCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto findCompilationById(@PathVariable Integer compId) {

        return compilationService.findCompilationById(compId);
    }
}
