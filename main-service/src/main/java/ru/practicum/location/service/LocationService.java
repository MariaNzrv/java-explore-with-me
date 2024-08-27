package ru.practicum.location.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.location.model.Location;
import ru.practicum.location.storage.LocationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    public Location findLocationById(LocationDto locationDto) {
        Float lat = locationDto.getLat();
        Float lon = locationDto.getLon();

        Location location = locationRepository.findByLatAndLon(lat, lon);
        if (location != null) {
            return location;
        } else {
            location = locationRepository.save(new Location(lat, lon));
            return location;
        }
    }
}
