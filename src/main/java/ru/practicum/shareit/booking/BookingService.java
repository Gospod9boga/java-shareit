package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {
    Booking createBooking(Booking booking);

    Booking approveBooking(Long bookingId, Long ownerId, boolean approved);

    Booking getBookingById(Long bookingId, Long userId);

    List<Booking> getUserBookings(Long userId, String state);

    List<Booking> getOwnerBookings(Long ownerId, String state);
}
