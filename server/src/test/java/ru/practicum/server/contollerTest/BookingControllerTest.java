package ru.practicum.server.contollerTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.server.booking.BookingController;
import ru.practicum.server.booking.BookingService;
import ru.practicum.server.booking.dto.BookingDto;
import ru.practicum.server.booking.dto.BookingResponseDto;
import ru.practicum.server.booking.BookingStatus;
import ru.practicum.server.item.dto.ItemDto;
import ru.practicum.server.user.dto.UserDto;
import ru.practicum.server.Exception.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;
    private BookingResponseDto bookingResponseDto;
    private ItemDto itemDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        userDto = new UserDto();
        userDto.setId(2L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setId(1L);
        bookingResponseDto.setStart(LocalDateTime.now().plusDays(1));
        bookingResponseDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingResponseDto.setItem(itemDto);
        bookingResponseDto.setBooker(userDto);
        bookingResponseDto.setStatus(BookingStatus.WAITING);
    }

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.item.id").value(1L))
                .andExpect(jsonPath("$.item.name").value("Test Item"))
                .andExpect(jsonPath("$.booker.id").value(2L))
                .andExpect(jsonPath("$.booker.name").value("Test User"))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void createBooking_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void createBooking_shouldReturnInternalServerErrorWhenInvalidUserId() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MethodArgumentTypeMismatchException")));
    }

    @Test
    void createBooking_shouldReturnBadRequestWhenServiceThrowsValidationException() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new ValidationException("Invalid booking dates"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid booking dates"));
    }

    @Test
    void createBooking_shouldReturnNotFoundWhenItemNotFound() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new EntityNotFoundException("Item not found"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    void createBooking_shouldReturnForbiddenWhenUserIsItemOwner() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new AccessDeniedException("Cannot book your own item"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Cannot book your own item"));
    }

    @Test
    void createBooking_shouldReturnBadRequestWhenItemNotAvailable() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new ValidationException("Item is not available for booking"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Item is not available for booking"));
    }

    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.item.id").value(1L))
                .andExpect(jsonPath("$.booker.id").value(2L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getBookingById_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void getBookingById_shouldReturnInternalServerErrorWhenInvalidPath() throws Exception {
        mockMvc.perform(get("/bookings/invalid")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MethodArgumentTypeMismatchException")));
    }

    @Test
    void getBookingById_shouldReturnNotFoundForInvalidId() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new EntityNotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Booking not found"));
    }

    @Test
    void getBookingById_shouldReturnForbiddenWhenAccessDenied() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Access denied to booking"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied to booking"));
    }

    @Test
    void approveBooking_shouldReturnApprovedBooking() throws Exception {
        BookingResponseDto approvedBooking = new BookingResponseDto();
        approvedBooking.setId(1L);
        approvedBooking.setItem(itemDto);
        approvedBooking.setBooker(userDto);
        approvedBooking.setStart(LocalDateTime.now().plusDays(1));
        approvedBooking.setEnd(LocalDateTime.now().plusDays(2));
        approvedBooking.setStatus(BookingStatus.APPROVED);

        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approveBooking_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void approveBooking_shouldReturnInternalServerErrorWhenInvalidPath() throws Exception {
        mockMvc.perform(patch("/bookings/invalid")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MethodArgumentTypeMismatchException")));
    }

    @Test
    void approveBooking_shouldReturnInternalServerErrorWhenInvalidParam() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "invalid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MethodArgumentTypeMismatchException")));
    }

    @Test
    void approveBooking_shouldReturnNotFoundForInvalidId() throws Exception {
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new EntityNotFoundException("Booking not found"));

        mockMvc.perform(patch("/bookings/999")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Booking not found"));
    }

    @Test
    void approveBooking_shouldReturnForbiddenWhenNotOwner() throws Exception {
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new AccessDeniedException("Only item owner can approve booking"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 999L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only item owner can approve booking"));
    }

    @Test
    void approveBooking_shouldReturnBadRequestWhenAlreadyApproved() throws Exception {
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new ValidationException("Booking already approved"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Booking already approved"));
    }

    @Test
    void getAllBookingsUsers_shouldReturnListOfBookings() throws Exception {
        BookingResponseDto booking2 = new BookingResponseDto();
        booking2.setId(2L);

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Item 2");

        UserDto user2 = new UserDto();
        user2.setId(3L);
        user2.setName("User 2");

        booking2.setItem(item2);
        booking2.setBooker(user2);
        booking2.setStart(LocalDateTime.now().plusDays(3));
        booking2.setEnd(LocalDateTime.now().plusDays(4));
        booking2.setStatus(BookingStatus.APPROVED);

        when(bookingService.getUserBookings(anyLong(), anyString()))
                .thenReturn(List.of(bookingResponseDto, booking2));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getAllBookingsUsers_shouldUseDefaultState() throws Exception {
        when(bookingService.getUserBookings(anyLong(), anyString()))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllBookingsUsers_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(get("/bookings")
                        .param("state", "ALL"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void getAllBookingsUsers_shouldReturnBadRequestWhenInvalidState() throws Exception {
        when(bookingService.getUserBookings(anyLong(), anyString()))
                .thenThrow(new ValidationException("Unknown state: INVALID_STATE"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: INVALID_STATE"));
    }

    @Test
    void getAllBookingsUsers_shouldReturnEmptyListForNoBookings() throws Exception {
        when(bookingService.getUserBookings(anyLong(), anyString()))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 999L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getOwnerBookings_shouldReturnListOfOwnerBookings() throws Exception {
        BookingResponseDto ownerBooking = new BookingResponseDto();
        ownerBooking.setId(3L);
        ownerBooking.setItem(itemDto);
        ownerBooking.setBooker(userDto);
        ownerBooking.setStart(LocalDateTime.now().plusDays(1));
        ownerBooking.setEnd(LocalDateTime.now().plusDays(2));
        ownerBooking.setStatus(BookingStatus.APPROVED);

        when(bookingService.getOwnerBookings(anyLong(), anyString()))
                .thenReturn(List.of(ownerBooking));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void getOwnerBookings_shouldUseDefaultState() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), anyString()))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOwnerBookings_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void getOwnerBookings_shouldReturnBadRequestWhenInvalidState() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), anyString()))
                .thenThrow(new ValidationException("Unknown state: INVALID"));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: INVALID"));
    }

    @Test
    void getOwnerBookings_shouldReturnForbiddenWhenNotOwner() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), anyString()))
                .thenThrow(new AccessDeniedException("No items found for user"));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 999L)
                        .param("state", "ALL"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No items found for user"));
    }

    @Test
    void getOwnerBookings_shouldReturnEmptyListForNoItems() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), anyString()))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 999L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createBooking_shouldReturnBadRequestWhenPastStartDate() throws Exception {
        BookingDto pastBooking = new BookingDto();
        pastBooking.setItemId(1L);
        pastBooking.setStart(LocalDateTime.now().minusDays(1));
        pastBooking.setEnd(LocalDateTime.now().plusDays(1));

        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new ValidationException("Start date cannot be in the past"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastBooking)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Start date cannot be in the past"));
    }

    @Test
    void createBooking_shouldReturnBadRequestWhenEndBeforeStart() throws Exception {
        BookingDto invalidBooking = new BookingDto();
        invalidBooking.setItemId(1L);
        invalidBooking.setStart(LocalDateTime.now().plusDays(2));
        invalidBooking.setEnd(LocalDateTime.now().plusDays(1));

        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new ValidationException("End date must be after start date"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBooking)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("End date must be after start date"));
    }

    @Test
    void approveBooking_shouldReturnRejectedBooking() throws Exception {
        BookingResponseDto rejectedBooking = new BookingResponseDto();
        rejectedBooking.setId(1L);
        rejectedBooking.setItem(itemDto);
        rejectedBooking.setBooker(userDto);
        rejectedBooking.setStart(LocalDateTime.now().plusDays(1));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(2));
        rejectedBooking.setStatus(BookingStatus.REJECTED);

        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(rejectedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }
}