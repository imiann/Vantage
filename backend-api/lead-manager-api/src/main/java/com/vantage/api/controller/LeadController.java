package com.vantage.api.controller;

import com.vantage.api.dto.LeadRequest;
import com.vantage.api.entity.Client;
import com.vantage.api.entity.Lead;
import com.vantage.api.service.ClientService;
import com.vantage.api.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
public class LeadController {
    private final LeadService leadService;
    private final ClientService clientService;

    public LeadController(LeadService leadService, ClientService clientService) {
        this.leadService = leadService;
        this.clientService = clientService;
    }

    @Operation(summary = "Create a new lead")
    @PostMapping
    public ResponseEntity<Lead> createLead(@RequestBody @Valid LeadRequest leadRequest) {
        Lead createdLead = leadService.createLead(
                leadRequest.name(),
                leadRequest.email(),
                leadRequest.phone(),
                leadRequest.company(),
                leadRequest.source(),
                leadRequest.notes(),
                leadRequest.status()
        );
        // Return 202 if good
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLead);
    }

    @Operation(summary = "List all leads, optionally filtered by status")
    @GetMapping
    public ResponseEntity<List<Lead>> getAllLeads(@RequestParam(required = false) Optional<Lead.LeadStatus> status) {
        return status.map(leadStatus -> ResponseEntity.ok(leadService.getLeadsByStatus(leadStatus)))
                .orElseGet(() -> ResponseEntity.ok(leadService.getAllLeads()));
    }

    @Operation(summary = "Get a single lead by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Lead> getLeadById(@PathVariable UUID id) {
        return ResponseEntity.ok(leadService.getLeadById(id));
    }

    @Operation(summary = "Update an existing lead by ID")
    @PutMapping("/{id}")
    public ResponseEntity<Lead> updateLead(@PathVariable UUID id, @RequestBody @Valid LeadRequest leadRequest) {
        Lead updated = leadService.updateLead(
                id,
                leadRequest.name(),
                leadRequest.email(),
                leadRequest.phone(),
                leadRequest.company(),
                leadRequest.source(),
                leadRequest.notes(),
                leadRequest.status());

        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a lead by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(@PathVariable UUID id) {
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all leads (development use only)")
    @DeleteMapping("/DELETEALL")
    public ResponseEntity<Void> deleteAllLeads() {
        leadService.deleteAllLeads();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update the status of a lead")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Lead> updateStatus(@PathVariable UUID id, @RequestBody Lead.LeadStatus status) {
        return ResponseEntity.ok(leadService.updateStatus(id, status));
    }

    @Operation(summary = "Convert a lead to a client")
    @PostMapping("/{id}/convert")
    public ResponseEntity<Client> convertLead(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.convertLeadToClient(id));
    }

}
