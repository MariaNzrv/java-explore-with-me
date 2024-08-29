package ru.practicum.category.mapper;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryMapper {
    public static CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }

    public static List<CategoryDto> toDto(List<Category> categories) {
        List<CategoryDto> categoryDtos = new ArrayList<>();
        for (Category category : categories) {
            categoryDtos.add(toDto(category));
        }
        return categoryDtos;
    }

    public static Category toEntity(NewCategoryDto newCategoryDto) {
        return new Category(
                newCategoryDto.getName()
        );
    }
}
