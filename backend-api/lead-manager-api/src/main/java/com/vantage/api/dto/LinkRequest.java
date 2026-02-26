package com.vantage.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

/**
 * Request payload for creating or updating an external link.
 *
 * @param url       The external URL (required).
 * @param projectId Optional FK to the owning project.
 * @param name      Optional human-readable label for the link.
 */
public record LinkRequest(
        @NotBlank(message = "URL is required") @URL(message = "Invalid URL format") String url,

        UUID projectId,

        String name) {
}
