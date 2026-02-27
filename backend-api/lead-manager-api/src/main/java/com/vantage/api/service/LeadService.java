package com.vantage.api.service;

import com.vantage.api.entity.Lead;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.repository.LeadRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LeadService {

    private final LeadRepository repository;

    public LeadService(LeadRepository repository) {
        this.repository = repository;
    }

    public Lead createLead(String name, String email, String phone, String company, String source, String notes, Lead.LeadStatus status) {
        Lead lead = new Lead();
        lead.setName(name);
        lead.setEmail(email);
        lead.setPhone(phone);
        lead.setCompany(company);
        lead.setSource(source);
        lead.setNotes(notes);
        lead.setStatus(status);
        return repository.save(lead);
    }

    public List<Lead> getAllLeads() {
        return repository.findAll();
    }

    public List<Lead> getLeadsByStatus(Lead.LeadStatus leadStatus) {
        return repository.findByStatus(leadStatus);
    }

    public Lead getLeadById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
    }

    public Lead updateLead(UUID id, String name, String email, String phone, String company, String source, String notes, Lead.LeadStatus status) {
        Lead lead = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        validateTerminalStatus(lead, "updated");

        lead.setName(name);
        lead.setEmail(email);
        lead.setPhone(phone);
        lead.setCompany(company);
        lead.setSource(source);
        lead.setNotes(notes);
        lead.setStatus(status);
        return repository.save(lead);
    }

    public void deleteLead(UUID id) {
        Lead lead = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        validateTerminalStatus(lead, "deleted");

        repository.deleteById(id);
    }

    public void deleteAllLeads() {
        repository.deleteAll();
    }

    public Lead updateStatus(UUID id, Lead.LeadStatus status) {
        Lead lead = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        validateTerminalStatus(lead, "updated");

        if (status == Lead.LeadStatus.CONVERTED) {
            throw new IllegalStateException("Lead status cannot be set to CONVERTED via this endpoint. Use convertLead instead.");
        }

        lead.setStatus(status);
        return repository.save(lead);
    }

    public Lead convertLead(UUID id) {
        Lead lead = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        validateTerminalStatus(lead, "converted");

        lead.setStatus(Lead.LeadStatus.CONVERTED);
        lead.setConvertedAt(java.time.LocalDateTime.now());
        return repository.save(lead);
    }

    private void validateTerminalStatus(Lead lead, String action) {
        if (lead.getStatus() == Lead.LeadStatus.CONVERTED) {
            throw new IllegalStateException("Lead cannot be " + action + " because it has already been converted.");
        }

        if (lead.getStatus() == Lead.LeadStatus.LOST) {
            throw new IllegalStateException("Lead cannot be " + action + " because it has already been lost.");
        }
    }
}
