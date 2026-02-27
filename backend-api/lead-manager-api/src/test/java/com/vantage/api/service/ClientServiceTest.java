package com.vantage.api.service;

import com.vantage.api.dto.ClientRequest;
import com.vantage.api.entity.Client;
import com.vantage.api.entity.Lead;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.repository.ClientRepository;
import com.vantage.api.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private LeadService leadService;

    @InjectMocks
    private ClientService clientService;

    private Client testClient;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        testClient = new Client();
        testClient.setId(clientId);
        testClient.setFullName("Test Client");
        testClient.setEmail("client@test.com");
        testClient.setCompany("Test Co");
    }

    @Test
    @DisplayName("Should create client successfully")
    void createClient_Success() {
        ClientRequest request = new ClientRequest("Test Client", "client@test.com", null, "Test Co", null, null, null, null, null, null);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        Client created = clientService.createClient(request);

        assertNotNull(created);
        assertEquals("Test Client", created.getFullName());
    }

    @Test
    @DisplayName("Should convert lead to client successfully")
    void convertLeadToClient_Success() {
        UUID leadId = UUID.randomUUID();
        Lead lead = new Lead();
        lead.setId(leadId);
        lead.setName("Lead Name");
        lead.setEmail("lead@test.com");
        lead.setCompany("Lead Co");

        when(leadService.getLeadById(leadId)).thenReturn(lead);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Client result = clientService.convertLeadToClient(leadId);

        assertNotNull(result);
        assertEquals("Lead Name", result.getFullName());
        assertEquals(leadId, result.getConvertedFromId());
        verify(leadService, times(1)).convertLead(leadId);
    }

    @Test
    @DisplayName("Should delete client if no projects exist")
    void deleteClient_Success() {
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(projectRepository.existsByClientId(clientId)).thenReturn(false);

        clientService.deleteClient(clientId);

        verify(clientRepository, times(1)).deleteById(clientId);
    }

    @Test
    @DisplayName("Should throw exception when deleting client with projects")
    void deleteClient_HasProjects_ThrowsException() {
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(projectRepository.existsByClientId(clientId)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> clientService.deleteClient(clientId));
    }
}
