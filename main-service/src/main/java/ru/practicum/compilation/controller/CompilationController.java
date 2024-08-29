package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

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
