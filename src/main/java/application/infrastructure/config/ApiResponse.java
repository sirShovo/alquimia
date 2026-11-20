package application.infrastructure.config;

import java.time.LocalDateTime;

/**
 * Generic wrapper for all API responses. Standardizes the format of successful and error responses.
 *
 * @param <T> the type of the payload data
 * @param success whether the request was successful
 * @param message a human-readable message about the result
 * @param data the payload (or error details)
 * @param timestamp the moment the response was generated
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message, T errorDetails) {
        return new ApiResponse<>(false, message, errorDetails, LocalDateTime.now());
    }
}
