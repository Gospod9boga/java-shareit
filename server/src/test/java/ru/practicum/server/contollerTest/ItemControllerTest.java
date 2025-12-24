package ru.practicum.server.contollerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.server.Exception.AccessDeniedException;
import ru.practicum.server.Exception.EmailAlreadyExistsException;
import ru.practicum.server.Exception.EntityNotFoundException;
import ru.practicum.server.Exception.ValidationException;
import ru.practicum.server.booking.dto.BookingShortDto;
import ru.practicum.server.comments.CommentDto;
import ru.practicum.server.comments.CommentResponseDto;
import ru.practicum.server.item.ItemController;
import ru.practicum.server.item.dto.ItemDto;
import ru.practicum.server.item.model.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private CommentResponseDto commentResponseDto;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(1L);
        commentResponseDto.setText("Great item!");
        commentResponseDto.setAuthorName("User");
        commentResponseDto.setCreated(LocalDateTime.now());
    }

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        when(itemService.createItem(any(ItemDto.class), anyLong()))
                .thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void createItem_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void createItem_shouldReturnInternalServerErrorWhenUserIdHeaderInvalid() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MethodArgumentTypeMismatchException")));
    }

    @Test
    void createItem_shouldReturnBadRequestWhenServiceThrowsValidationException() throws Exception {
        when(itemService.createItem(any(ItemDto.class), anyLong()))
                .thenThrow(new ValidationException("Validation error"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"));
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void getItemById_shouldReturnNotFoundForInvalidId() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenThrow(new EntityNotFoundException("Item not found"));

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    void getItemById_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void getUserItems_shouldReturnListOfItems() throws Exception {
        ItemDto itemDto2 = new ItemDto();
        itemDto2.setId(2L);
        itemDto2.setName("Item 2");
        itemDto2.setDescription("Desc 2");
        itemDto2.setAvailable(true);

        when(itemService.getUserItems(anyLong()))
                .thenReturn(List.of(itemDto, itemDto2));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getUserItems_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        ItemDto updatedItem = new ItemDto();
        updatedItem.setId(1L);
        updatedItem.setName("Updated Name");
        updatedItem.setDescription("Updated Description");
        updatedItem.setAvailable(false);

        when(itemService.updateItem(anyLong(), any(ItemDto.class), anyLong()))
                .thenReturn(updatedItem);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void updateItem_shouldReturnForbiddenWhenNotOwner() throws Exception {
        when(itemService.updateItem(anyLong(), any(ItemDto.class), anyLong()))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    @Test
    void updateItem_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void searchItems_shouldReturnMatchingItems() throws Exception {
        when(itemService.searchItems(anyString()))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void searchItems_shouldReturnEmptyListForBlankText() throws Exception {
        when(itemService.searchItems(anyString()))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchItems_shouldReturnInternalServerErrorWhenTextParameterMissing() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingServletRequestParameterException")));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        when(itemService.addComment(anyLong(), any(CommentDto.class), anyLong()))
                .thenReturn(commentResponseDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("User"));
    }

    @Test
    void addComment_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void addComment_shouldReturnBadRequestWhenEmptyComment() throws Exception {
        CommentDto emptyComment = new CommentDto();
        emptyComment.setText("");

        when(itemService.addComment(anyLong(), any(CommentDto.class), anyLong()))
                .thenThrow(new ValidationException("Comment text cannot be empty"));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyComment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Comment text cannot be empty"));
    }

    @Test
    void getItemById_shouldIncludeBookingsForOwner() throws Exception {
        BookingShortDto lastBooking = new BookingShortDto();
        lastBooking.setId(1L);
        lastBooking.setBookerId(2L);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));

        BookingShortDto nextBooking = new BookingShortDto();
        nextBooking.setId(2L);
        nextBooking.setBookerId(3L);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));

        itemDto.setLastBooking(lastBooking);
        itemDto.setNextBooking(nextBooking);

        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastBooking").exists())
                .andExpect(jsonPath("$.nextBooking").exists())
                .andExpect(jsonPath("$.lastBooking.id").value(1L))
                .andExpect(jsonPath("$.nextBooking.id").value(2L));
    }

    @Test
    void getUserItems_shouldIncludeCommentsAndBookings() throws Exception {
        itemDto.setComments(List.of(commentResponseDto));

        BookingShortDto lastBooking = new BookingShortDto();
        lastBooking.setId(1L);
        lastBooking.setBookerId(2L);
        itemDto.setLastBooking(lastBooking);

        when(itemService.getUserItems(anyLong()))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comments").isArray())
                .andExpect(jsonPath("$[0].comments.length()").value(1))
                .andExpect(jsonPath("$[0].lastBooking").exists())
                .andExpect(jsonPath("$[0].lastBooking.id").value(1L));
    }

    @Test
    void createItem_shouldReturnConflictForDuplicateItem() throws Exception {
        when(itemService.createItem(any(ItemDto.class), anyLong()))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }
}