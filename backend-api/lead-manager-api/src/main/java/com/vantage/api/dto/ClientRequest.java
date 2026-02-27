package com.vantage.api.dto;

import com.vantage.api.entity.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        String phone,
        @NotBlank(message = "Company is required") String company,
        String companyNumber,
        String address,
        String logoUrl,
        String primaryContact,
        String notes,
        Client.ClientStatus status
) {
}
