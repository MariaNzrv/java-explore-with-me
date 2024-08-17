package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

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

    public List<StatsResponse> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("start", URLEncoder.encode(start.toString(), StandardCharsets.UTF_8));
            map.put("end", URLEncoder.encode(end.toString(), StandardCharsets.UTF_8));
            map.put("uris", uris);
            map.put("unique", unique);
            ResponseEntity<StatsResponse[]> response = restTemplate.getForEntity(serverUrl + "/stats", StatsResponse[].class, map);
//        ResponseEntity<StatsResponse> response = restTemplate
//                .exchange(serverUrl + "/stats?start=" + URLEncoder.encode(start, StandardCharsets.UTF_8) +
//                        "&end=" + URLEncoder.encode(end, StandardCharsets.UTF_8) +
//                        "&uris=" + Arrays.toString(uris) +
//                        "&unique=" + unique, HttpMethod.GET, null, StatsResponse.class);

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

    public void postHit(HitRequest hitRequest) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(serverUrl + "/hit", hitRequest, Void.class);
            if (!response.getStatusCode().equals(HttpStatusCode.valueOf(201))) {
                log.error("Код ответа: {}", response.getStatusCode());
                throw new IllegalStateException("Код ошибки: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Во время выполнения запроса POST по URL-адресу: {} , возникла ошибка.", serverUrl + "/hit");
            throw new IllegalArgumentException("Во время выполнения запроса POST по URL-адресу: '" + serverUrl + "/hit" + "', возникла ошибка.\n");
        }

        //HttpEntity<HitRequest> request = new HttpEntity<>(hitRequest);
        //ResponseEntity<Void> response = restTemplate.exchange(serverUrl, HttpMethod.POST, request, Void.class);
    }
}
