package com.vantage.api.controller;

import com.vantage.api.dto.ClientRequest;
import com.vantage.api.entity.Client;
import com.vantage.api.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Operation(summary = "Create a new client manually")
    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody @Valid ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(request));
    }

    @Operation(summary = "List all clients")
    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @Operation(summary = "Get a single client by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable UUID id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @Operation(summary = "Update an existing client by ID")
    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable UUID id, @RequestBody @Valid ClientRequest request) {
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }

    @Operation(summary = "Delete a client by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update the status of a client")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Client> updateStatus(@PathVariable UUID id, @RequestBody Client.ClientStatus status) {
        return ResponseEntity.ok(clientService.updateStatus(id, status));
    }
}
