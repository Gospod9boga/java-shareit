package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.AccessDeniedException;
import ru.practicum.shareit.Exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private final ItemService itemService;
    private Long currentId = 0L;

    public BookingServiceImpl(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public Booking createBooking(Booking booking) {
        Item item = itemService.getItemById(booking.getItemId());

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available");
        }
        if (item.getOwnerId().equals(booking.getBookerId())) {
            throw new AccessDeniedException("Cannot book your own item");
        }

        booking.setId(currentId);
        booking.setStatus(BookingStatus.WAITING);
        bookings.put(currentId, booking);
        currentId++;
        return booking;
    }

    @Override
    public Booking approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) throw new ValidationException("Booking not found");

        Item item = itemService.getItemById(booking.getItemId());
        if (!item.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Only owner can approve booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking already processed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return booking;
    }

    @Override
    public Booking getBookingById(Long bookingId, Long userId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) throw new ValidationException("Booking not found");

        Item item = itemService.getItemById(booking.getItemId());
        boolean isBooker = booking.getBookerId().equals(userId);
        boolean isOwner = item.getOwnerId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new AccessDeniedException("Not allowed to view");
        }
        return booking;
    }

    @Override
    public List<Booking> getUserBookings(Long userId, String state) {
        List<Booking> userBookings = getAllBookingsByUserId(userId);
        return filterBookingsByState(userBookings, state);
    }

    @Override
    public List<Booking> getOwnerBookings(Long ownerId, String state) {
        List<Booking> ownerBookings = getAllBookingsByOwnerId(ownerId);
        return filterBookingsByState(ownerBookings, state);
    }

    private List<Booking> getAllBookingsByUserId(Long userId) {
        return bookings.values().stream()
                .filter(booking -> booking.getBookerId().equals(userId))
                .collect(Collectors.toList());
    }

    private List<Booking> getAllBookingsByOwnerId(Long ownerId) {
        return bookings.values().stream()
                .filter(booking -> {
                    Item item = itemService.getItemById(booking.getItemId());
                    return item.getOwnerId().equals(ownerId);
                })
                .collect(Collectors.toList());
    }

    private List<Booking> filterBookingsByState(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case "ALL":
                return bookings;
            case "CURRENT":
                return bookings.stream()
                        .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                        .collect(Collectors.toList());
            case "PAST":
                return bookings.stream()
                        .filter(b -> b.getEnd().isBefore(now))
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookings.stream()
                        .filter(b -> b.getStart().isAfter(now))
                        .collect(Collectors.toList());
            case "WAITING":
            case "REJECTED":
                return bookings.stream()
                        .filter(b -> b.getStatus().name().equals(state))
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }
}
