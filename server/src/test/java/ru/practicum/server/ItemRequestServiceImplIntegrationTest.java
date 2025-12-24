package ru.practicum.server;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.practicum.server.item.model.Item;
import ru.practicum.server.item.ItemRepository;
import ru.practicum.server.request.ItemRequest;
import ru.practicum.server.request.ItemRequestImplService;
import ru.practicum.server.request.ItemRequestRepository;
import ru.practicum.server.user.User;
import ru.practicum.server.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ItemRequestImplService.class})
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestImplService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = createUser("User 1", "user1@test.com");
        user2 = createUser("User 2", "user2@test.com");
        user3 = createUser("User 3", "user3@test.com");

        createRequest("Need a drill", user1, LocalDateTime.now().minusDays(3));
        createRequest("Need a hammer", user1, LocalDateTime.now().minusDays(2));
        createRequest("Need a saw", user2, LocalDateTime.now().minusDays(1));
        createRequest("Need a ladder", user3, LocalDateTime.now());
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequestsWithPagination() {
        List<ItemRequest> result = itemRequestService.getAllRequests(user1.getId(), 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ItemRequest::getDescription)
                .containsExactlyInAnyOrder("Need a saw", "Need a ladder");
    }

    @Test
    void getAllRequests_shouldPaginateCorrectly() {
        for (int i = 4; i <= 15; i++) {
            createRequest("Request " + i, user2, LocalDateTime.now().plusHours(i));
        }

        List<ItemRequest> firstPage = itemRequestService.getAllRequests(user1.getId(), 0, 5);
        List<ItemRequest> secondPage = itemRequestService.getAllRequests(user1.getId(), 5, 5);

        assertThat(firstPage).hasSize(5);
        assertThat(secondPage).hasSize(5);

        List<Long> firstPageIds = firstPage.stream().map(ItemRequest::getId).toList();
        List<Long> secondPageIds = secondPage.stream().map(ItemRequest::getId).toList();

        assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
    }

    @Test
    void getAllRequests_shouldNotReturnOwnRequests() {
        List<ItemRequest> result = itemRequestService.getAllRequests(user1.getId(), 0, 10);

        assertThat(result)
                .extracting(ItemRequest::getDescription)
                .doesNotContain("Need a drill", "Need a hammer");
    }


    @Test
    void getAllRequests_shouldHandleFromGreaterThanTotal() {
        List<ItemRequest> result = itemRequestService.getAllRequests(user1.getId(), 10, 5);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllRequests_shouldSortByCreatedDesc() {
        List<ItemRequest> result = itemRequestService.getAllRequests(user1.getId(), 0, 10);

        for (int i = 0; i < result.size() - 1; i++) {
            LocalDateTime current = result.get(i).getCreated();
            LocalDateTime next = result.get(i + 1).getCreated();
            assertThat(current).isAfterOrEqualTo(next);
        }
    }


    @Test
    void getAllRequests_shouldThrowExceptionForNonExistentUser() {
        assertThrows(RuntimeException.class,
                () -> itemRequestService.getAllRequests(999L, 0, 10));
    }

    @Test
    void getAllRequests_shouldHandleZeroSize() {
        List<ItemRequest> result = itemRequestService.getAllRequests(user1.getId(), 0, 0);

        assertThat(result).isEmpty();
    }


    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private ItemRequest createRequest(String description, User requester, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(requester);
        request.setCreated(created);
        return itemRequestRepository.save(request);
    }
}