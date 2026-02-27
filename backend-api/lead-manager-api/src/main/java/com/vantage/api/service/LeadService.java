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
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Lead", id);
        }
        // If status is "CONVERTED" reject with 409
        Lead lead = repository.findById(id).get();
        if (lead.getStatus() == Lead.LeadStatus.CONVERTED) {
            throw new IllegalStateException("Lead cannot be deleted because it has already been converted.");
        }
        repository.deleteById(id);
    }

    public void deleteAllLeads() {
        repository.deleteAll();
    }

    public Lead updateStatus(UUID id, Lead.LeadStatus status) {
        Lead lead = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        if (lead.getStatus() == Lead.LeadStatus.CONVERTED) {
            throw new IllegalStateException("Lead cannot be updated because it has already been converted.");
        }

        if (lead.getStatus() == Lead.LeadStatus.LOST) {
            throw new IllegalStateException("Lead cannot be updated because it has already been lost.");
        }

        lead.setStatus(status);
        return repository.save(lead);
    }

    public Lead convertLead(UUID id) {
        Lead lead = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        if (lead.getStatus() == Lead.LeadStatus.CONVERTED) {
            throw new IllegalStateException("Lead cannot be converted because it has already been converted.");
        }

        if (lead.getStatus() == Lead.LeadStatus.LOST) {
            throw new IllegalStateException("Lead cannot be converted because it has already been lost.");
        }

        lead.setStatus(Lead.LeadStatus.CONVERTED);
        lead.setConvertedAt(java.time.LocalDateTime.now());
        return repository.save(lead);
    }
}
