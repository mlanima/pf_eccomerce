package com.horseriding.ecommerce.common.dtos.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for successful operation confirmations.
 * @param <T> The type of data being returned (optional)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponse<T> {

    private String message;
    private int status;
    private LocalDateTime timestamp;
    private T data;

    public SuccessResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public SuccessResponse(String message, int status, T data) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }
}