package com.vantage.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * Request payload for creating a new external link.
 */
public record LinkRequest(
                @NotBlank(message = "URL is required") @URL(message = "Invalid URL format") String url) {
}
