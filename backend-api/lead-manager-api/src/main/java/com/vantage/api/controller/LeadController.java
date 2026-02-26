package com.vantage.api.controller;

import com.vantage.api.service.LeadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
public class LeadController {
    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    /**
    @PostMapping
    public ResponseEntity<LeadResponse> createLead() {
        leadService.createLead();
        return null;
    }

    @GetMapping
    public void getAllLeads() {
        leadService.getAllLeads();
    }

    @GetMapping
    public void getLeadById() {
        leadService.getLeadById();
    }

    @PutMapping
    public void updateLead() {
        leadService.updateLead();
    }

    @DeleteMapping
    public void deleteLead() {
        leadService.deleteLead();
    }

    @DeleteMapping
    public void deleteAllLeads() {
        leadService.deleteAllLeads();
    }

    @PatchMapping
    public void updateStatus() {
        leadService.updateStatus();
    }

    @PostMapping
    public void convertLead() {
        leadService.convertLead();
    }
    **/
}
