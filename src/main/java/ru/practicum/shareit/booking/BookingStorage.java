package ru.practicum.shareit.booking;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BookingStorage {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private Long currentId = 0L;

    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(++currentId);
        }
        bookings.put(booking.getId(), booking);
        return booking;
    }


    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }


    public void deleteById(Long id) {
        bookings.remove(id);
    }


    public List<Booking> findAll() {
        return new ArrayList<>(bookings.values());
    }


    public List<Booking> findByBookerId(Long bookerId) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookings.values()) {
            if (booking.getBookerId().equals(bookerId)) {
                result.add(booking);
            }
        }
        return result;
    }


    public List<Booking> findByStatus(BookingStatus status) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookings.values()) {
            if (booking.getStatus() == status) {
                result.add(booking);
            }
        }
        return result;
    }


    public boolean existsById(Long id) {
        return bookings.containsKey(id);
    }
}
