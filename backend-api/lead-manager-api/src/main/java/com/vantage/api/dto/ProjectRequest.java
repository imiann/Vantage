package com.vantage.api.dto;

import com.vantage.api.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ProjectRequest(
        @NotNull(message = "Client ID is required") UUID clientId,
        @NotBlank(message = "Title is required") String title,
        String description,
        Project.ProjectStatus status,
        BigDecimal price,
        String currency,
        String notes
) {
}
