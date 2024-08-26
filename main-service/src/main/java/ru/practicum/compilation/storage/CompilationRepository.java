package ru.practicum.compilation.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.model.Event;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Integer> {
    List<Compilation> findAllByPinned(Boolean pinned, Pageable page);
}
