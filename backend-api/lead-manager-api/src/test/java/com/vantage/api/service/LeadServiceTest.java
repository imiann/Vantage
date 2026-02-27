package com.vantage.api.service;

import com.vantage.api.entity.Lead;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeadServiceTest {

    @Mock
    private LeadRepository repository;

    @InjectMocks
    private LeadService leadService;

    private Lead testLead;
    private UUID leadId;

    @BeforeEach
    void setUp() {
        leadId = UUID.randomUUID();
        testLead = new Lead();
        testLead.setId(leadId);
        testLead.setName("John Doe");
        testLead.setEmail("john@example.com");
        testLead.setStatus(Lead.LeadStatus.INBOUND);
    }

    @Test
    @DisplayName("Should create lead successfully")
    void createLead_Success() {
        when(repository.save(any(Lead.class))).thenReturn(testLead);

        Lead created = leadService.createLead("John Doe", "john@example.com", "123456", "Vantage", "Google", "Notes", Lead.LeadStatus.INBOUND);

        assertNotNull(created);
        assertEquals("John Doe", created.getName());
        verify(repository, times(1)).save(any(Lead.class));
    }

    @Test
    @DisplayName("Should get all leads")
    void getAllLeads_Success() {
        when(repository.findAll()).thenReturn(List.of(testLead));

        List<Lead> leads = leadService.getAllLeads();

        assertFalse(leads.isEmpty());
        assertEquals(1, leads.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get lead by ID")
    void getLeadById_Success() {
        when(repository.findById(leadId)).thenReturn(Optional.of(testLead));

        Lead found = leadService.getLeadById(leadId);

        assertNotNull(found);
        assertEquals(leadId, found.getId());
    }

    @Test
    @DisplayName("Should throw exception when lead not found")
    void getLeadById_NotFound() {
        when(repository.findById(leadId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leadService.getLeadById(leadId));
    }

    @Test
    @DisplayName("Should update status successfully")
    void updateStatus_Success() {
        when(repository.findById(leadId)).thenReturn(Optional.of(testLead));
        when(repository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead updated = leadService.updateStatus(leadId, Lead.LeadStatus.FOLLOW_UP);

        assertEquals(Lead.LeadStatus.FOLLOW_UP, updated.getStatus());
        verify(repository, times(1)).save(any(Lead.class));
    }

    @Test
    @DisplayName("Should throw exception when updating status from terminal state (CONVERTED)")
    void updateStatus_FromConverted_ThrowsException() {
        testLead.setStatus(Lead.LeadStatus.CONVERTED);
        when(repository.findById(leadId)).thenReturn(Optional.of(testLead));

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
                () -> leadService.updateStatus(leadId, Lead.LeadStatus.FOLLOW_UP));
        
        assertTrue(ex.getMessage().contains("already been converted"));
    }

    @Test
    @DisplayName("Should throw exception when updating status from terminal state (LOST)")
    void updateStatus_FromLost_ThrowsException() {
        testLead.setStatus(Lead.LeadStatus.LOST);
        when(repository.findById(leadId)).thenReturn(Optional.of(testLead));

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
                () -> leadService.updateStatus(leadId, Lead.LeadStatus.FOLLOW_UP));
        
        assertTrue(ex.getMessage().contains("already been lost"));
    }

    @Test
    @DisplayName("Should convert lead successfully")
    void convertLead_Success() {
        when(repository.findById(leadId)).thenReturn(Optional.of(testLead));
        when(repository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead converted = leadService.convertLead(leadId);

        assertEquals(Lead.LeadStatus.CONVERTED, converted.getStatus());
        assertNotNull(converted.getConvertedAt());
        verify(repository, times(1)).save(any(Lead.class));
    }

    @Test
    @DisplayName("Should throw exception when converting terminal lead")
    void convertLead_Terminal_ThrowsException() {
        testLead.setStatus(Lead.LeadStatus.LOST);
        when(repository.findById(leadId)).thenReturn(Optional.of(testLead));

        assertThrows(IllegalStateException.class, () -> leadService.convertLead(leadId));
    }

    @Test
    @DisplayName("Should throw exception when updating status to CONVERTED via updateStatus")
    void updateStatus_ToConverted_ThrowsException() {
        when(repository.findById(leadId)).thenReturn(Optional.of(testLead));

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
                () -> leadService.updateStatus(leadId, Lead.LeadStatus.CONVERTED));
        
        assertTrue(ex.getMessage().contains("Use convertLead"));
    }

    @Test
    @DisplayName("Should throw exception when updating terminal lead via updateLead")
    void updateLead_Terminal_ThrowsException() {
        testLead.setStatus(Lead.LeadStatus.CONVERTED);
        when(repository.findById(leadId)).thenReturn(Optional.of(testLead));

        assertThrows(IllegalStateException.class, 
                () -> leadService.updateLead(leadId, "New Name", "new@example.com", null, null, null, null, Lead.LeadStatus.INBOUND));
    }

    @Test
    @DisplayName("Should delete lead successfully")
    void deleteLead_Success() {
        when(repository.findById(leadId)).thenReturn(Optional.of(testLead));

        leadService.deleteLead(leadId);

        verify(repository, times(1)).deleteById(leadId);
    }

    @Test
    @DisplayName("Should throw exception when deleting converted lead")
    void deleteLead_Converted_ThrowsException() {
        testLead.setStatus(Lead.LeadStatus.CONVERTED);
        when(repository.findById(leadId)).thenReturn(Optional.of(testLead));

        assertThrows(IllegalStateException.class, () -> leadService.deleteLead(leadId));
    }
}
