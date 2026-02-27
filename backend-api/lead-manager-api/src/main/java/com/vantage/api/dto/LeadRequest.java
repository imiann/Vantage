package com.vantage.api.dto;

import com.vantage.api.entity.Lead;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LeadRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        String phone,
        String company,
        String source,
        String notes,
        Lead.LeadStatus status
) {
}
