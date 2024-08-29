package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> findAll(@RequestParam(defaultValue = "0", required = false, name = "from") Integer from,
                                     @RequestParam(defaultValue = "10", required = false, name = "size") Integer size) {
        return CategoryMapper.toDto(categoryService.findAll(from, size));
    }

    @GetMapping("/{catId}")
    public CategoryDto findById(@PathVariable Integer catId) {
        return CategoryMapper.toDto(categoryService.findCategoryById(catId));
    }
}
