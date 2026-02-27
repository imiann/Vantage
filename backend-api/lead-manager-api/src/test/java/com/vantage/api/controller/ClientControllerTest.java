package com.vantage.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantage.api.dto.ClientRequest;
import com.vantage.api.entity.Client;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.service.ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/clients should return 201 Created")
    void createClient_ReturnsCreated() throws Exception {
        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setFullName("John Doe");

        when(clientService.createClient(any(ClientRequest.class))).thenReturn(client);

        ClientRequest request = new ClientRequest("John Doe", "john@example.com", null, "Doe Co", null, null, null, null, null, null);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/clients should return 200 OK")
    void getAllClients_ReturnsOk() throws Exception {
        Client client = new Client();
        client.setFullName("John Doe");

        when(clientService.getAllClients()).thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/clients/{id} should return 200 OK")
    void getClientById_ReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        Client client = new Client();
        client.setId(id);
        client.setFullName("John Doe");

        when(clientService.getClientById(id)).thenReturn(client);

        mockMvc.perform(get("/api/clients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/clients/{id} should return 404 Not Found")
    void getClientById_ReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(clientService.getClientById(id)).thenThrow(new ResourceNotFoundException("Client", id));

        mockMvc.perform(get("/api/clients/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} should return 204 No Content")
    void deleteClient_ReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(clientService).deleteClient(id);

        mockMvc.perform(delete("/api/clients/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} should return 409 Conflict when has projects")
    void deleteClient_HasProjects_ReturnsConflict() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalStateException("Has projects")).when(clientService).deleteClient(id);

        mockMvc.perform(delete("/api/clients/{id}", id))
                .andExpect(status().isConflict());
    }
}
