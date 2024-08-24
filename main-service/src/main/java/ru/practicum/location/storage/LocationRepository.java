package ru.practicum.location.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.model.Event;
import ru.practicum.location.model.Location;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    Location findByLatAndLon(Float lat, Float lon);
}
