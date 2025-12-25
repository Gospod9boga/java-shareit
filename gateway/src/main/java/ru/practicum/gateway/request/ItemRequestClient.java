package ru.practicum.gateway.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.gateway.client.BaseClient;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    private final String serverUrl;

    public ItemRequestClient(RestTemplateBuilder builder,
                             @Value("${server.host:localhost:9090}") String host) {
        super(builder.build());
        this.serverUrl = "http://" + host;
    }

    public ResponseEntity<Object> createRequest(Long userId, ItemRequestDto requestDto) {
        String url = serverUrl + "/requests";
        return post(url, userId, requestDto);
    }

    public ResponseEntity<Object> getUserRequests(Long userId) {
        String url = serverUrl + "/requests";
        return get(url, userId);
    }

    public ResponseEntity<Object> getAllRequests(Long userId, Integer from, Integer size) {
        String url = serverUrl + "/requests/all?from={from}&size={size}";
        Map<String, Object> parameters = Map.of("from", from, "size", size);
        return get(url, userId, parameters);
    }

    public ResponseEntity<Object> getRequestById(Long userId, Long requestId) {
        String url = serverUrl + "/requests/" + requestId;
        return get(url, userId);
    }
}
