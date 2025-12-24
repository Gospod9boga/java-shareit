package ru.practicum.server;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.practicum.server.Exception.EntityNotFoundException;
import ru.practicum.server.Exception.ValidationException;
import ru.practicum.server.booking.Booking;
import ru.practicum.server.booking.BookingRepository;
import ru.practicum.server.booking.BookingStatus;
import ru.practicum.server.booking.dto.BookingResponseDto;
import ru.practicum.server.booking.BookingServiceImpl;
import ru.practicum.server.item.ItemRepository;
import ru.practicum.server.item.model.Item;
import ru.practicum.server.user.User;
import ru.practicum.server.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BookingServiceImpl.class})
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker1;
    private User booker2;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {

        owner = createUser("Owner", "owner@test.com");
        booker1 = createUser("Booker 1", "booker1@test.com");
        booker2 = createUser("Booker 2", "booker2@test.com");

        item1 = createItem("Item 1", "Description 1", true, owner);
        item2 = createItem("Item 2", "Description 2", true, owner);

        createBooking(item1, booker1,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(8),
                BookingStatus.APPROVED);

        createBooking(item1, booker2,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(4),
                BookingStatus.APPROVED);

        createBooking(item2, booker1,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                BookingStatus.APPROVED);

        createBooking(item2, booker2,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(7),
                BookingStatus.WAITING);

        createBooking(item1, booker1,
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(12),
                BookingStatus.REJECTED);

        createBooking(item2, booker2,
                LocalDateTime.now().minusDays(15),
                LocalDateTime.now().minusDays(13),
                BookingStatus.APPROVED);
    }

    @Test
    void getOwnerBookings_shouldReturnAllBookings() {

        List<BookingResponseDto> result = bookingService.getOwnerBookings(
                owner.getId(), "ALL");

        assertThat(result).hasSize(6);

        assertThat(result)
                .extracting(bookingDto -> bookingDto.getItem().getName())
                .containsOnly("Item 1", "Item 2");
    }

    @Test
    void getOwnerBookings_shouldReturnCurrentBookings() {

        List<BookingResponseDto> result = bookingService.getOwnerBookings(
                owner.getId(), "CURRENT");

        assertThat(result).hasSize(1);

        BookingResponseDto currentBooking = result.get(0);
        assertThat(currentBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(currentBooking.getItem().getName()).isEqualTo("Item 2");
    }

    @Test
    void getOwnerBookings_shouldReturnPastBookings() {

        List<BookingResponseDto> result = bookingService.getOwnerBookings(
                owner.getId(), "PAST");

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(BookingResponseDto::getStatus)
                .containsOnly(BookingStatus.APPROVED);
    }

    @Test
    void getOwnerBookings_shouldReturnFutureBookings() {

        List<BookingResponseDto> result = bookingService.getOwnerBookings(
                owner.getId(), "FUTURE");

        assertThat(result).hasSize(3);

        List<BookingStatus> statuses = result.stream()
                .map(BookingResponseDto::getStatus)
                .toList();

        assertThat(statuses).containsExactlyInAnyOrder(
                BookingStatus.APPROVED,
                BookingStatus.WAITING,
                BookingStatus.REJECTED
        );
    }

    @Test
    void getOwnerBookings_shouldReturnWaitingBookings() {

        List<BookingResponseDto> result = bookingService.getOwnerBookings(
                owner.getId(), "WAITING");

        assertThat(result).hasSize(1);

        BookingResponseDto waitingBooking = result.get(0);
        assertThat(waitingBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(waitingBooking.getItem().getName()).isEqualTo("Item 2");
    }

    @Test
    void getOwnerBookings_shouldReturnRejectedBookings() {

        List<BookingResponseDto> result = bookingService.getOwnerBookings(
                owner.getId(), "REJECTED");

        assertThat(result).hasSize(1);

        BookingResponseDto rejectedBooking = result.get(0);
        assertThat(rejectedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(rejectedBooking.getItem().getName()).isEqualTo("Item 1");
    }

    @Test
    void getOwnerBookings_shouldReturnEmptyForUserWithoutItems() {

        User userWithoutItems = createUser("No Items", "noitems@test.com");

        List<BookingResponseDto> result = bookingService.getOwnerBookings(
                userWithoutItems.getId(), "ALL");

        assertThat(result).isEmpty();
    }

    @Test
    void getOwnerBookings_shouldThrowExceptionForInvalidState() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getOwnerBookings(owner.getId(), "INVALID")
        );

        assertThat(exception.getMessage()).contains("Unknown state");
    }

    @Test
    void getOwnerBookings_shouldFilterCorrectly() {

        List<BookingResponseDto> futureBookings = bookingService.getOwnerBookings(
                owner.getId(), "FUTURE");

        assertThat(futureBookings).hasSize(3);

        for (BookingResponseDto booking : futureBookings) {
            assertThat(booking.getStart()).isAfter(LocalDateTime.now());
        }
    }

    @Test
    void getOwnerBookings_shouldReturnSortedByStartDateDesc() {

        List<BookingResponseDto> result = bookingService.getOwnerBookings(
                owner.getId(), "ALL");

        assertThat(result).hasSize(6);

        for (int i = 0; i < result.size() - 1; i++) {
            LocalDateTime currentStart = result.get(i).getStart();
            LocalDateTime nextStart = result.get(i + 1).getStart();

            assertThat(currentStart).isAfterOrEqualTo(nextStart);
        }
    }

    @Test
    void getOwnerBookings_shouldThrowExceptionForNonExistentUser() {
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getOwnerBookings(999L, "ALL")
        );

        assertThat(exception.getMessage()).contains("Пользователь не найден");
    }

    @Test
    void getOwnerBookings_shouldThrowExceptionForNullOwnerId() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getOwnerBookings(null, "ALL")
        );

        assertThat(exception.getMessage()).contains("ID владельца не указан");
    }

    @Test
    void getOwnerBookings_shouldHandleLowerCaseState() {

        List<BookingResponseDto> result = bookingService.getOwnerBookings(
                owner.getId(), "future");

        assertThat(result).hasSize(3);
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item createItem(String name, String description, Boolean available, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setIsAvailable(available);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private Booking createBooking(Item item, User booker, LocalDateTime start,
                                  LocalDateTime end, BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
}