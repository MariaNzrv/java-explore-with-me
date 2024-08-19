package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.App;

import java.util.Optional;

public interface AppRepository extends JpaRepository<App, Integer> {
    Optional<App> findByName(String name);
}
