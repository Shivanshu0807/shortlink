package com.shortlink.shortlink.dto;

public record ShortUrlResponse(
        String shortCode,
        String shortUrl,
        String originalUrl
) {
}