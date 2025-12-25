package ru.practicum.gateway.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.gateway.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private final String serverUrl;

    public BookingClient(RestTemplateBuilder builder,
                         @Value("${server.host:localhost:9090}") String host) {
        super(builder.build());
        this.serverUrl = "http://" + host;
    }

    public ResponseEntity<Object> createBooking(Long userId, BookingRequestDto bookingDto) {
        String url = serverUrl + "/bookings";
        return post(url, userId, bookingDto);
    }

    public ResponseEntity<Object> getBooking(Long userId, Long bookingId) {
        String url = serverUrl + "/bookings/" + bookingId;
        return get(url, userId);
    }

    public ResponseEntity<Object> approveBooking(Long userId, Long bookingId, boolean approved) {
        String url = serverUrl + "/bookings/" + bookingId + "?approved={approved}";
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch(url, userId, parameters, null);
    }

    public ResponseEntity<Object> getUserBookings(Long userId, String state) {
        String url = serverUrl + "/bookings?state={state}";
        Map<String, Object> parameters = Map.of("state", state);
        return get(url, userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(Long ownerId, String state) {
        String url = serverUrl + "/bookings/owner?state={state}";
        Map<String, Object> parameters = Map.of("state", state);
        return get(url, ownerId, parameters);
    }
}