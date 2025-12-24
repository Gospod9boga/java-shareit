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
import ru.practicum.server.Exception.EntityNotFoundException;
import ru.practicum.server.Exception.ValidationException;
import ru.practicum.server.request.ItemRequest;
import ru.practicum.server.request.ItemRequestController;
import ru.practicum.server.request.ItemRequestService;
import ru.practicum.server.request.dto.ItemRequestDto;
import ru.practicum.server.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto itemRequestDto;
    private ItemRequestResponseDto itemRequestResponseDto;
    private ItemRequest mockRequest;

    @BeforeEach
    void setUp() {
        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Need a drill for home repairs");

        itemRequestResponseDto = new ItemRequestResponseDto();
        itemRequestResponseDto.setId(1L);
        itemRequestResponseDto.setDescription("Need a drill for home repairs");
        itemRequestResponseDto.setCreated(LocalDateTime.now());
        itemRequestResponseDto.setItems(List.of());

        mockRequest = new ItemRequest();
        mockRequest.setId(1L);
        mockRequest.setDescription("Need a drill for home repairs");
        mockRequest.setCreated(LocalDateTime.now());
    }

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.createRequest(any(ItemRequest.class), anyLong()))
                .thenReturn(mockRequest);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill for home repairs"));
    }

    @Test
    void createRequest_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void createRequest_shouldReturnInternalServerErrorWhenInvalidUserId() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MethodArgumentTypeMismatchException")));
    }

    @Test
    void createRequest_shouldReturnInternalServerErrorWhenEmptyDescription() throws Exception {
        ItemRequestDto emptyRequest = new ItemRequestDto();
        emptyRequest.setDescription("");

        when(itemRequestService.createRequest(any(ItemRequest.class), anyLong()))
                .thenReturn(null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("NullPointerException")));
    }

    @Test
    void createRequest_shouldReturnInternalServerErrorWhenNullDescription() throws Exception {
        ItemRequestDto nullRequest = new ItemRequestDto();
        nullRequest.setDescription(null);

        when(itemRequestService.createRequest(any(ItemRequest.class), anyLong()))
                .thenReturn(null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("NullPointerException")));
    }

    @Test
    void createRequest_shouldReturnBadRequestWhenServiceThrowsValidationException() throws Exception {
        when(itemRequestService.createRequest(any(ItemRequest.class), anyLong()))
                .thenThrow(new ValidationException("Invalid request data"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request data"));
    }

    @Test
    void getUserRequests_shouldReturnListOfRequests() throws Exception {
        when(itemRequestService.getUserRequests(anyLong()))
                .thenReturn(List.of(mockRequest, mockRequest));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUserRequests_shouldReturnEmptyList() throws Exception {
        when(itemRequestService.getUserRequests(anyLong()))
                .thenReturn(List.of());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUserRequests_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void getAllRequests_shouldReturnPaginatedRequests() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(mockRequest, mockRequest));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllRequests_shouldUseDefaultPagination() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(mockRequest));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllRequests_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void getAllRequests_shouldReturnOkWhenInvalidPagination() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(mockRequest);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill for home repairs"));
    }

    @Test
    void getRequestById_shouldReturnNotFoundForInvalidId() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new EntityNotFoundException("Request not found"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Request not found"));
    }

    @Test
    void getRequestById_shouldReturnInternalServerErrorWhenNoUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MissingRequestHeaderException")));
    }

    @Test
    void getRequestById_shouldReturnInternalServerErrorWhenInvalidPath() throws Exception {
        mockMvc.perform(get("/requests/invalid")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(containsString("MethodArgumentTypeMismatchException")));
    }

    @Test
    void getRequestById_shouldReturnForbiddenWhenAccessDenied() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Access denied to this request"));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied to this request"));
    }

    @Test
    void getUserRequests_shouldReturnForbiddenWhenAccessDenied() throws Exception {
        when(itemRequestService.getUserRequests(anyLong()))
                .thenThrow(new AccessDeniedException("Cannot access user requests"));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Cannot access user requests"));
    }

    @Test
    void getAllRequests_shouldReturnForbiddenWhenAccessDenied() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenThrow(new AccessDeniedException("Cannot access all requests"));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Cannot access all requests"));
    }

    @Test
    void createRequest_shouldReturnForbiddenWhenAccessDenied() throws Exception {
        when(itemRequestService.createRequest(any(ItemRequest.class), anyLong()))
                .thenThrow(new AccessDeniedException("Cannot create request"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Cannot create request"));
    }
}