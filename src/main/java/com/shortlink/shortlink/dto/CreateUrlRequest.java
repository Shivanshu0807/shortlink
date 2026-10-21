package com.shortlink.shortlink.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUrlRequest(
        @NotBlank(message = "originalUrl is required")
        @Size(max = 2048, message = "originalUrl is too long")
        String originalUrl,

        @Min(value = 1, message = "expiresInDays must be at least 1")
        Integer expiresInDays
) {
}