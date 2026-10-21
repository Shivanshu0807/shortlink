package com.shortlink.shortlink.service;

import com.shortlink.shortlink.dto.CreateUrlRequest;
import com.shortlink.shortlink.dto.ShortUrlResponse;
import com.shortlink.shortlink.entity.Url;
import com.shortlink.shortlink.repository.UrlRepository;
import com.shortlink.shortlink.util.Base62;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ShortUrlResponse create(CreateUrlRequest request) {
        String original = request.originalUrl().trim();
        validateUrl(original);

        Url url = new Url();
        url.setOriginalUrl(original);
        if (request.expiresInDays() != null) {
            url.setExpiresAt(LocalDateTime.now().plusDays(request.expiresInDays()));
        }

        url = urlRepository.save(url);
        url.setShortCode(Base62.encode(url.getId()));

        return new ShortUrlResponse(
                url.getShortCode(),
                baseUrl + "/" + url.getShortCode(),
                url.getOriginalUrl());
    }

    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Short URL not found"));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Short URL has expired");
        }
        return url.getOriginalUrl();
    }

    private void validateUrl(String raw) {
        try {
            URI uri = new URI(raw);
            String scheme = uri.getScheme();
            boolean validScheme = scheme != null
                    && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
            if (!validScheme || uri.getHost() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Only valid http/https URLs are allowed");
            }
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL format");
        }
    }
}