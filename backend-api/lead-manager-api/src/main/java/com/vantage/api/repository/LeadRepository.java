package com.vantage.api.repository;


import com.vantage.api.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
    // Count Bys
    long countByStatus(Lead.LeadStatus status);
    long countBySource(String source);


    // Find Bys
    List<Lead> findByStatus(Lead.LeadStatus status);
    List<Lead> findByFollowUpDateBefore(java.time.LocalDateTime date);
    List<Lead> findByFollowUpDateBeforeAndStatusNotIn(java.time.LocalDateTime date, List<Lead.LeadStatus> statuses);
    List<Lead> findByFollowUpDateAfter(java.time.LocalDateTime date);
    List<Lead> findByFollowUpDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Lead> findBySource(String source);
    List<Lead> findByNameContaining(String name);
    List<Lead> findByEmailContaining(String email);
    List<Lead> findByPhoneContaining(String phone);
}
