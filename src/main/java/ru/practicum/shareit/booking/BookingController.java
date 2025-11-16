package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;

import java.util.stream.Collectors;

import java.util.List;


@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @PostMapping
    public BookingDto createBooking(@Valid @RequestBody BookingDto bookingDto) {
        log.info("Create booking:{} ", bookingDto);
        Booking booking = BookingMapper.toEntity(bookingDto);
        Booking savedBooking = bookingService.createBooking(booking);
        return BookingMapper.toDto(savedBooking);

    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get booking by id: {}", bookingId);
        Booking booking = bookingService.getBookingById(bookingId, userId);
        return BookingMapper.toDto(booking);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approvedBooking(@PathVariable Long bookingId,
                                      @RequestParam Boolean approved,
                                      @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Update booking by id:{}", bookingId);
        Booking booking = bookingService.approveBooking(bookingId, userId, approved);
        return BookingMapper.toDto(booking);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsUsers(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state
    ) {
        log.info("All bookings for user: {}, state: {}", userId, state);
        List<Booking> bookings = bookingService.getUserBookings(userId, state);
        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "ALL") String state
    ) {
        log.info("Owner bookings for user: {}, state: {}", ownerId, state);
        List<Booking> bookings = bookingService.getOwnerBookings(ownerId, state);
        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

}
