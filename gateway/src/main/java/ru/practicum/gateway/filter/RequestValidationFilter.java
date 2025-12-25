package ru.practicum.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class RequestValidationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> VALID_BOOKING_STATES = Set.of(
            "ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.debug("Processing request: {} {}", method, path);

        if (isProtectedPath(path)) {
            if (!validateProtectedRequest(request, path, response)) {
                return;
            }
        }

        if (!validatePathVariables(path, response)) {
            return;
        }

        String validationError = validateQueryParameters(request, path);
        if (validationError != null) {
            sendErrorResponse(response, validationError, HttpStatus.BAD_REQUEST);
            return;
        }

        log.debug("Request validation passed: {} {}", method, path);
        filterChain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path) {
        return !path.equals("/users") &&
                !(path.startsWith("/users") && !path.matches("/users/\\d+"));
    }

    private boolean validateProtectedRequest(HttpServletRequest request,
                                             String path,
                                             HttpServletResponse response) throws IOException {
        String userIdStr = request.getHeader(USER_ID_HEADER);

        if (!StringUtils.hasText(userIdStr)) {
            log.warn("Missing header {} for path: {}", USER_ID_HEADER, path);
            sendErrorResponse(response,
                    "Missing required header: " + USER_ID_HEADER,
                    HttpStatus.BAD_REQUEST);
            return false;
        }

        try {
            long userId = Long.parseLong(userIdStr);
            if (userId <= 0) {
                log.warn("Invalid userId (must be positive): {} for path: {}", userIdStr, path);
                sendErrorResponse(response,
                        "User ID must be positive",
                        HttpStatus.BAD_REQUEST);
                return false;
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid userId format (must be number): {} for path: {}", userIdStr, path);
            sendErrorResponse(response,
                    "User ID must be a valid number",
                    HttpStatus.BAD_REQUEST);
            return false;
        }

        return true;
    }

    private boolean validatePathVariables(String path, HttpServletResponse response) throws IOException {
        if (path.matches(".*/\\d+.*")) {
            String[] parts = path.split("/");
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    try {
                        long id = Long.parseLong(part);
                        if (id <= 0) {
                            log.warn("Invalid ID in path (must be positive): {}", path);
                            sendErrorResponse(response,
                                    "Invalid ID in path. ID must be positive",
                                    HttpStatus.BAD_REQUEST);
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format in path: {}", path);
                        sendErrorResponse(response,
                                "Invalid ID format in path",
                                HttpStatus.BAD_REQUEST);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private String validateQueryParameters(HttpServletRequest request, String path) {
        if (path.startsWith("/bookings") && !path.contains("/owner")) {
            String state = request.getParameter("state");
            if (state != null && !isValidBookingState(state)) {
                return "Invalid booking state: '" + state + "'. " +
                        "Valid values: ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED";
            }
        }
        if (path.startsWith("/bookings/owner")) {
            String state = request.getParameter("state");
            if (state != null && !isValidBookingState(state)) {
                return "Invalid booking state for owner: '" + state + "'";
            }
        }
        if (path.equals("/items") || path.startsWith("/items/search")) {
            String from = request.getParameter("from");
            String size = request.getParameter("size");

            if (from != null && !isNonNegativeInteger(from)) {
                return "Parameter 'from' must be a non-negative integer";
            }

            if (size != null && !isPositiveInteger(size)) {
                return "Parameter 'size' must be a positive integer";
            }
            if (path.startsWith("/items/search")) {
                String text = request.getParameter("text");
                if (text != null && text.trim().isEmpty()) {
                    return "Search text cannot be empty";
                }
            }
        }
        if (path.startsWith("/requests/all")) {
            String from = request.getParameter("from");
            String size = request.getParameter("size");

            if (from != null && !isNonNegativeInteger(from)) {
                return "Parameter 'from' must be a non-negative integer";
            }

            if (size != null && !isPositiveInteger(size)) {
                return "Parameter 'size' must be a positive integer";
            }
        }
        if (path.matches("/bookings/\\d+") && "PATCH".equals(request.getMethod())) {
            String approved = request.getParameter("approved");
            if (approved == null) {
                return "Missing required parameter: approved";
            }
            if (!"true".equalsIgnoreCase(approved) && !"false".equalsIgnoreCase(approved)) {
                return "Parameter 'approved' must be 'true' or 'false'";
            }
        }

        return null;
    }

    private boolean isValidBookingState(String state) {
        return VALID_BOOKING_STATES.contains(state.toUpperCase());
    }

    private boolean isNonNegativeInteger(String value) {
        try {
            int num = Integer.parseInt(value);
            return num >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isPositiveInteger(String value) {
        try {
            int num = Integer.parseInt(value);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   String message,
                                   HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorBody = Map.of(
                "error", status.getReasonPhrase(),
                "message", message,
                "timestamp", Instant.now().toString(),
                "status", status.value()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
