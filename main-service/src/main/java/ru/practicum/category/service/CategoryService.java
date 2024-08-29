package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.storage.CategoryRepository;
import ru.practicum.error.exception.ConflictValidationException;
import ru.practicum.error.exception.EntityNotFoundException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.event.model.Event;
import ru.practicum.event.storage.EventRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public Category create(NewCategoryDto newCategoryDto) {
        String name = newCategoryDto.getName();
        validateFieldsFormat(name, null);

        Category category = CategoryMapper.toEntity(newCategoryDto);

        return categoryRepository.save(category);
    }

    public void delete(Integer catId) {
        validateCategoryId(catId);
        Category category = findCategoryById(catId);
        List<Event> events = eventRepository.findAllByCategoryId(catId);
        if (events != null && !events.isEmpty()) {
            log.warn("Существуют события, связанные с категорией");
            throw new ConflictValidationException("Существуют события, связанные с категорией");
        }
        categoryRepository.delete(category);
    }

    public Category update(Integer catId, NewCategoryDto newCategoryDto) {
        validateCategoryId(catId);
        validateFieldsFormat(newCategoryDto.getName(), catId);

        Category category = findCategoryById(catId);

        if (newCategoryDto.getName() != null) {
            category.setName(newCategoryDto.getName());
        }
        return categoryRepository.save(category);
    }

    public Category findCategoryById(Integer catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> {
            log.error("Категория с Id = {} не существует", catId);
            throw new EntityNotFoundException("Категория с Id = " + catId + " не существует");
        });
    }

    public List<Category> findAll(Integer from, Integer size) {
        Pageable page = getPageable(from, size);

        return categoryRepository.findAll(page).getContent();
    }

    private void validateFieldsFormat(String name, Integer catId) {
        if (name == null) {
            log.warn("Обязательные поля не заполнены");
            throw new ValidationException("Обязательные поля не заполнены");
        }

        if (name.isBlank()) {
            log.warn("Название не может состоять из пробелов");
            throw new ValidationException("Название не может состоять из пробелов");
        }

        if (name.isEmpty()) {
            log.warn("Название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }

        if (name.length() > 50) {
            log.warn("Название не может состоять больше, чем из 50-ти символов");
            throw new ValidationException("Название не может состоять больше, чем из 50-ти символов");
        }

        Category sameCategory = categoryRepository.findByName(name);
        if (sameCategory != null && (!sameCategory.getId().equals(catId))) {
            log.warn("Категория с таким названием уже есть в системе");
            throw new ConflictValidationException("Категория с таким названием уже есть в системе");
        }

    }

    private void validateCategoryId(Integer catId) {
        if (catId == null) {
            log.error("Id не заполнен");
            throw new ValidationException("Для удаления данных надо указать Id");
        }
    }

    private Pageable getPageable(Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            log.error("Некорректные значения параметров from = {}, size={}", from, size);
            throw new ValidationException("Некорректные значения параметров from/size");
        }

        Sort sortByEnd = Sort.by(Sort.Direction.ASC, "id");
        return PageRequest.of(from / size, size, sortByEnd);
    }

}
