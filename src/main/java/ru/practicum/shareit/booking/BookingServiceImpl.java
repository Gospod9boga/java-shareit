package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.AccessDeniedException;
import ru.practicum.shareit.Exception.ValidationException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final ItemService itemService;
    private final BookingStorage bookingStorage;
    private final ItemMapper itemMapper;

    public BookingServiceImpl(ItemService itemService, BookingStorage bookingStorage, ItemMapper itemMapper) {
        this.itemService = itemService;
        this.bookingStorage = bookingStorage;
        this.itemMapper = itemMapper;
    }

    @Override
    public BookingDto createBooking(BookingDto bookingDto, Long bookerId) {
        ItemDto itemDto = itemService.getItemById(bookingDto.getItemId());
        if (!itemDto.getAvailable()) {
            throw new ValidationException("Item is not available");
        }
        if (itemDto.getOwnerId().equals(bookerId)) {
            throw new AccessDeniedException("Cannot book your own item");
        }


        Booking booking = BookingMapper.toEntity(bookingDto);
        booking.setBookerId(bookerId);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingStorage.save(booking);
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new ValidationException("Booking not found"));

        ItemDto itemDto = itemService.getItemById(booking.getItemId());
        if (!itemDto.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Only owner can approve booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking already processed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingStorage.save(booking);
        return BookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new ValidationException("Booking not found"));

        ItemDto itemDto = itemService.getItemById(booking.getItemId());
        boolean isBooker = booking.getBookerId().equals(userId);
        boolean isOwner = itemDto.getOwnerId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new AccessDeniedException("Not allowed to view");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state) {
        List<Booking> userBookings = getAllBookingsByUserId(userId);
        List<Booking> filteredBookings = filterBookingsByState(userBookings, state);
        return filteredBookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, String state) {
        List<Booking> ownerBookings = getAllBookingsByOwnerId(ownerId);
        List<Booking> filteredBookings = filterBookingsByState(ownerBookings, state);
        return filteredBookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    // Остальные методы без изменений
    private List<Booking> getAllBookingsByUserId(Long userId) {
        return bookingStorage.findAll().stream()
                .filter(booking -> booking.getBookerId().equals(userId))
                .collect(Collectors.toList());
    }

    private List<Booking> getAllBookingsByOwnerId(Long ownerId) {
        return bookingStorage.findAll().stream()
                .filter(booking -> {
                    ItemDto itemDto = itemService.getItemById(booking.getItemId());
                    return itemDto.getOwnerId().equals(ownerId);
                })
                .collect(Collectors.toList());
    }

    private List<Booking> filterBookingsByState(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
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
                        .filter(b -> b.getStatus().name().equals(state.toUpperCase()))
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }
}
