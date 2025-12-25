package ru.practicum.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.practicum.server.booking.Booking;
import ru.practicum.server.booking.BookingRepository;
import ru.practicum.server.booking.BookingStatus;
import ru.practicum.server.comments.Comment;
import ru.practicum.server.comments.repo.CommentsRepository;
import ru.practicum.server.item.ItemRepository;
import ru.practicum.server.item.dto.ItemDto;
import ru.practicum.server.item.model.Item;
import ru.practicum.server.item.model.ItemImplService;
import ru.practicum.server.user.User;
import ru.practicum.server.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ItemImplService.class})
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemImplService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    private User owner;
    private User booker;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {

        owner = createUser("Owner", "owner@test.com");
        booker = createUser("Booker", "booker@test.com");

        item1 = createItem("Drill", "Powerful drill", true, owner);
        item2 = createItem("Hammer", "Professional hammer", true, owner);

        createBooking(item1, booker,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1),
                BookingStatus.APPROVED);

        createBooking(item1, booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                BookingStatus.APPROVED);

        createBooking(item2, booker,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(4),
                BookingStatus.APPROVED);

        createComment(item1, booker, "Great drill!");
        createComment(item1, booker, "Works perfectly");
        createComment(item2, booker, "Good hammer");
    }


    @Test
    void getUserItems_shouldReturnItemsWithBookingsAndComments() {

        List<ItemDto> result = itemService.getUserItems(owner.getId());

        assertThat(result).hasSize(2); // Две вещи у owner

        ItemDto drillItem = result.get(0);
        assertThat(drillItem.getName()).isEqualTo("Drill");
        assertThat(drillItem.getComments()).hasSize(2); // Два комментария
        assertThat(drillItem.getLastBooking()).isNotNull(); // Есть прошлое бронирование
        assertThat(drillItem.getNextBooking()).isNotNull(); // Есть будущее бронирование

        ItemDto hammerItem = result.get(1);
        assertThat(hammerItem.getName()).isEqualTo("Hammer");
        assertThat(hammerItem.getComments()).hasSize(1); // Один комментарий
        assertThat(hammerItem.getLastBooking()).isNotNull(); // Есть прошлое бронирование
        assertThat(hammerItem.getNextBooking()).isNull(); // Нет будущего бронирования
    }

    @Test
    void getUserItems_shouldReturnEmptyListForUserWithoutItems() {

        User newUser = createUser("New User", "new@test.com");


        List<ItemDto> result = itemService.getUserItems(newUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getUserItems_shouldReturnItemsWithoutBookings() {

        User userWithoutBookings = createUser("No Bookings", "nobookings@test.com");
        Item item = createItem("Item without bookings", "Test", true, userWithoutBookings);
        createComment(item, booker, "Test comment");

        List<ItemDto> result = itemService.getUserItems(userWithoutBookings.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastBooking()).isNull();
        assertThat(result.get(0).getNextBooking()).isNull();
        assertThat(result.get(0).getComments()).hasSize(1);
    }

    @Test
    void getUserItems_shouldReturnUnavailableItems() {

        Item unavailableItem = createItem("Broken Item", "Not working", false, owner);

        List<ItemDto> result = itemService.getUserItems(owner.getId());

        assertThat(result).hasSize(3);

        ItemDto brokenItem = result.stream()
                .filter(item -> item.getName().equals("Broken Item"))
                .findFirst()
                .orElse(null);

        assertThat(brokenItem).isNotNull();
        assertThat(brokenItem.getAvailable()).isFalse();
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

    private Booking createBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private Comment createComment(Item item, User author, String text) {
        Comment comment = new Comment();
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setText(text);
        return commentsRepository.save(comment);
    }
}