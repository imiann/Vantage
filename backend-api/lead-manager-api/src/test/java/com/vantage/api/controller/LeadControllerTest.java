package com.vantage.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantage.api.dto.LeadRequest;
import com.vantage.api.entity.Lead;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.service.LeadService;
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

@WebMvcTest(LeadController.class)
public class LeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeadService leadService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/leads should return 201 Created")
    void createLead_ReturnsCreated() throws Exception {
        Lead lead = new Lead();
        lead.setId(UUID.randomUUID());
        lead.setName("John Doe");
        lead.setEmail("john@example.com");

        when(leadService.createLead(anyString(), anyString(), any(), any(), any(), any(), any()))
                .thenReturn(lead);

        LeadRequest request = new LeadRequest("John Doe", "john@example.com", null, null, null, null, Lead.LeadStatus.INBOUND);

        mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /api/leads should return 200 OK")
    void getAllLeads_ReturnsOk() throws Exception {
        Lead lead = new Lead();
        lead.setName("John Doe");

        when(leadService.getAllLeads()).thenReturn(List.of(lead));

        mockMvc.perform(get("/api/leads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/leads/{id} should return 200 OK")
    void getLeadById_ReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        Lead lead = new Lead();
        lead.setId(id);
        lead.setName("John Doe");

        when(leadService.getLeadById(id)).thenReturn(lead);

        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/leads/{id} should return 404 Not Found")
    void getLeadById_ReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(leadService.getLeadById(id)).thenThrow(new ResourceNotFoundException("Lead", id));

        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/leads/{id} should return 200 OK")
    void updateLead_ReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        Lead lead = new Lead();
        lead.setId(id);
        lead.setName("Updated Name");

        when(leadService.updateLead(eq(id), anyString(), anyString(), any(), any(), any(), any(), any()))
                .thenReturn(lead);

        LeadRequest request = new LeadRequest("Updated Name", "updated@example.com", null, null, null, null, Lead.LeadStatus.INBOUND);

        mockMvc.perform(put("/api/leads/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("DELETE /api/leads/{id} should return 204 No Content")
    void deleteLead_ReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(leadService).deleteLead(id);

        mockMvc.perform(delete("/api/leads/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/leads/{id}/status should return 200 OK")
    void updateStatus_ReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        Lead lead = new Lead();
        lead.setId(id);
        lead.setStatus(Lead.LeadStatus.FOLLOW_UP);

        when(leadService.updateStatus(eq(id), any())).thenReturn(lead);

        mockMvc.perform(patch("/api/leads/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"FOLLOW_UP\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FOLLOW_UP"));
    }

    @Test
    @DisplayName("POST /api/leads/{id}/convert should return 200 OK")
    void convertLead_ReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        Lead lead = new Lead();
        lead.setId(id);
        lead.setStatus(Lead.LeadStatus.CONVERTED);

        when(leadService.convertLead(id)).thenReturn(lead);

        mockMvc.perform(post("/api/leads/{id}/convert", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONVERTED"));
    }

    @Test
    @DisplayName("POST /api/leads/{id}/convert should return 409 Conflict when terminal")
    void convertLead_Terminal_ReturnsConflict() throws Exception {
        UUID id = UUID.randomUUID();
        when(leadService.convertLead(id)).thenThrow(new IllegalStateException("Already converted"));

        mockMvc.perform(post("/api/leads/{id}/convert", id))
                .andExpect(status().isConflict());
    }
}
