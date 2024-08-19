package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.HitRequestDto;
import ru.practicum.stats.dto.StatsResponseDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(
            @Autowired RestTemplate restTemplate, @Value("${stat-server.url}") String serverUrl
    ) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    public List<StatsResponseDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("start", URLEncoder.encode(start.toString(), StandardCharsets.UTF_8));
            map.put("end", URLEncoder.encode(end.toString(), StandardCharsets.UTF_8));
            map.put("uris", uris);
            map.put("unique", unique);
            ResponseEntity<StatsResponseDto[]> response = restTemplate.getForEntity(serverUrl + "/stats", StatsResponseDto[].class, map);

            if (!response.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
                log.error("Код ответа: {}", response.getStatusCode());
                throw new IllegalStateException("Код ошибки: " + response.getStatusCode());
            }
            if (response.getBody() == null) {
                return new ArrayList<>();
            }
            return Arrays.asList(response.getBody());
        } catch (RestClientException e) {
            log.error("Во время выполнения запроса GET по URL-адресу: {} , возникла ошибка.", serverUrl + "/stats");
            throw new IllegalArgumentException("Во время выполнения запроса GET по URL-адресу: '" + serverUrl + "/stats" + "', возникла ошибка.\n");
        }
    }

    public void postHit(HitRequestDto hitRequestDto) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(serverUrl + "/hit", hitRequestDto, Void.class);
            if (!response.getStatusCode().equals(HttpStatusCode.valueOf(201))) {
                log.error("Код ответа: {}", response.getStatusCode());
                throw new IllegalStateException("Код ошибки: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Во время выполнения запроса POST по URL-адресу: {} , возникла ошибка.", serverUrl + "/hit");
            throw new IllegalArgumentException("Во время выполнения запроса POST по URL-адресу: '" + serverUrl + "/hit" + "', возникла ошибка.\n");
        }
    }
}
