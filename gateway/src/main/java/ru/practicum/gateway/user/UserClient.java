package ru.practicum.gateway.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.gateway.client.BaseClient;

@Slf4j
@Service
public class UserClient extends BaseClient {
    private final String serverUrl;

    public UserClient(@Value("${server.host:localhost:9090}") String host,
                      RestTemplate restTemplate) {
        super(restTemplate);
        this.serverUrl = "http://" + host;
        log.info("UserClient initialized with server URL: {}", serverUrl);
    }

    public ResponseEntity<Object> createUser(UserRequestDto userDto) {
        String url = serverUrl + "/users";
        return post(url, userDto);
    }

    public ResponseEntity<Object> getUser(Long userId) {
        String url = serverUrl + "/users/" + userId;
        return get(url);
    }

    public ResponseEntity<Object> updateUser(Long userId, UserUpdateDto userDto) {
        String url = serverUrl + "/users/" + userId;
        return patch(url, userDto);
    }

    public ResponseEntity<Object> getAllUsers() {
        String url = serverUrl + "/users";
        return get(url);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        String url = serverUrl + "/users/" + userId;
        return delete(url);
    }
}