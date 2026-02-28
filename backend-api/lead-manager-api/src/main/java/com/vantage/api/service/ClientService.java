package com.vantage.api.service;

import com.vantage.api.dto.ClientRequest;
import com.vantage.api.entity.Client;
import com.vantage.api.entity.Lead;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.repository.ClientRepository;
import com.vantage.api.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final LeadService leadService;

    public ClientService(ClientRepository clientRepository, ProjectRepository projectRepository, LeadService leadService) {
        this.clientRepository = clientRepository;
        this.projectRepository = projectRepository;
        this.leadService = leadService;
    }

    /**
     * Create a new client manually.
     */
    @Transactional
    public Client createClient(ClientRequest request) {
        Client client = new Client();
        mapRequestToClient(request, client);
        return clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    @Transactional
    public Client updateClient(UUID id, ClientRequest request) {
        Client client = getClientById(id);
        mapRequestToClient(request, client);
        return clientRepository.save(client);
    }

    @Transactional
    public void deleteClient(UUID id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client", id);
        }
        if (projectRepository.existsByClientId(id)) {
            throw new IllegalStateException("Client cannot be deleted because they have associated projects. Use ARCHIVED status instead.");
        }
        clientRepository.deleteById(id);
    }

    @Transactional
    public Client updateStatus(UUID id, Client.ClientStatus status) {
        Client client = getClientById(id);
        client.setStatus(status);
        return clientRepository.save(client);
    }

    /**
     * Convert a Lead into a Client.
     */
    @Transactional
    public Client convertLeadToClient(UUID leadId) {
        Lead lead = leadService.getLeadById(leadId);
        
        // This converts it
        leadService.convertLead(leadId);

        Client client = new Client();
        client.setFullName(lead.getName());
        client.setEmail(lead.getEmail());
        client.setPhone(lead.getPhone());
        client.setCompany(lead.getCompany() != null ? lead.getCompany() : "Individual");
        client.setNotes(lead.getNotes());
        client.setConvertedFromId(leadId);
        client.setStatus(Client.ClientStatus.ACTIVE);

        return clientRepository.save(client);
    }

    private void mapRequestToClient(ClientRequest request, Client client) {
        client.setFullName(request.fullName());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client.setCompany(request.company());
        client.setCompanyNumber(request.companyNumber());
        client.setAddress(request.address());
        client.setLogoUrl(request.logoUrl());
        client.setPrimaryContact(request.primaryContact());
        client.setNotes(request.notes());
        if (request.status() != null) {
            client.setStatus(request.status());
        }
    }
}
