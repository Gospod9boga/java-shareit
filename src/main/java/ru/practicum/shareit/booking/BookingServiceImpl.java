package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.Exception.AccessDeniedException;
import ru.practicum.shareit.Exception.EntityNotFoundException;
import ru.practicum.shareit.Exception.ValidationException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }


    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingDto bookingDto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Вещь с ID " + bookingDto.getItemId() + " не найдена"));

        if (!item.getIsAvailable()) {
            throw new ValidationException("Вещь " + item.getName() + " недоступна для бронирования");
        }

        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Пользователь не может бронировать свою вещь");
        }

        Booking booking = new Booking();
        booking.setStartDate(bookingDto.getStart());
        booking.setEndDate(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);
        booking.setBooker(user);

        Booking savedBooking = bookingRepository.save(booking);

        return BookingMapper.toResponseDto(savedBooking);
    }


    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Только владелец вещи может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);

        return BookingMapper.toResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new AccessDeniedException("Нет доступа к бронированию");
        }

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state) {

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        List<Booking> userBookings = bookingRepository.findByBookerIdOrderByStartDateDesc(userId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> filteredBookings;

        switch (state.toUpperCase()) {
            case "ALL":
                filteredBookings = userBookings;
                break;
            case "CURRENT":
                filteredBookings = userBookings.stream()
                        .filter(b -> b.getStartDate().isBefore(now) && b.getEndDate().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case "PAST":
                filteredBookings = userBookings.stream()
                        .filter(b -> b.getEndDate().isBefore(now))
                        .collect(Collectors.toList());
                break;
            case "FUTURE":
                filteredBookings = userBookings.stream()
                        .filter(b -> b.getStartDate().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case "WAITING":
            case "REJECTED":
                BookingStatus status = BookingStatus.valueOf(state.toUpperCase());
                filteredBookings = userBookings.stream()
                        .filter(b -> b.getStatus() == status)
                        .collect(Collectors.toList());
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return filteredBookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state) {
        if (ownerId == null) {
            throw new ValidationException("ID владельца не указан");
        }

        userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        List<Item> ownerItems = itemRepository.findByOwnerId(ownerId);

        if (ownerItems.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = ownerItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Booking> ownerBookings = bookingRepository.findByItemIdInOrderByStartDateDesc(itemIds);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> filteredBookings;

        switch (state.toUpperCase()) {
            case "ALL":
                filteredBookings = ownerBookings;
                break;
            case "CURRENT":
                filteredBookings = ownerBookings.stream()
                        .filter(b -> b.getStartDate().isBefore(now) && b.getEndDate().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case "PAST":
                filteredBookings = ownerBookings.stream()
                        .filter(b -> b.getEndDate().isBefore(now))
                        .collect(Collectors.toList());
                break;
            case "FUTURE":
                filteredBookings = ownerBookings.stream()
                        .filter(b -> b.getStartDate().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case "WAITING":
            case "REJECTED":
                BookingStatus status = BookingStatus.valueOf(state.toUpperCase());
                filteredBookings = ownerBookings.stream()
                        .filter(b -> b.getStatus() == status)
                        .collect(Collectors.toList());
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return filteredBookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }


    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                  LocalDateTime start2, LocalDateTime end2) {

        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
}
