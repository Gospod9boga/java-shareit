package ru.practicum.gateway.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.gateway.client.BaseClient;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private final String serverUrl;

    public ItemClient(RestTemplateBuilder builder,
                      @Value("${server.host:localhost:9090}") String host) {
        super(builder.build());
        this.serverUrl = "http://" + host;
    }

    public ResponseEntity<Object> createItem(Long userId, ItemRequestDto itemDto) {
        String url = serverUrl + "/items";
        return post(url, userId, itemDto);
    }

    public ResponseEntity<Object> getItem(Long userId, Long itemId) {
        String url = serverUrl + "/items/" + itemId;
        return get(url, userId);
    }

    public ResponseEntity<Object> updateItem(Long userId, Long itemId, ItemRequestDto itemDto) {
        String url = serverUrl + "/items/" + itemId;
        return patch(url, userId, null, itemDto);
    }

    public ResponseEntity<Object> getUserItems(Long userId, Integer from, Integer size) {
        String url = serverUrl + "/items?from={from}&size={size}";
        Map<String, Object> parameters = Map.of("from", from, "size", size);
        return get(url, userId, parameters);
    }

    public ResponseEntity<Object> searchItems(Long userId, String text, Integer from, Integer size) {
        String url = serverUrl + "/items/search?text={text}&from={from}&size={size}";
        Map<String, Object> parameters = Map.of("text", text, "from", from, "size", size);
        return get(url, userId, parameters);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentRequestDto commentDto) {
        String url = serverUrl + "/items/" + itemId + "/comment";
        return post(url, userId, commentDto);
    }
}
