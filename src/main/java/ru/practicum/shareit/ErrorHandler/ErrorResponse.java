package ru.practicum.shareit.ErrorHandler;

import java.time.LocalDateTime;

public class ErrorResponse {
    private String error;
    private LocalDateTime timestamp;

    public ErrorResponse(String error) {
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }


    public String getError() {
        return error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
