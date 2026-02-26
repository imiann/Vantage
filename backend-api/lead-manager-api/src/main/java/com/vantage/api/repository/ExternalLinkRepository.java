package com.vantage.api.repository;

import com.vantage.api.entity.ExternalLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 *  Handles all database operations for the ExternalLink entity.
 *  JpaRepository provides save, find, and delete methods out of the box.
 */

@Repository
public interface ExternalLinkRepository extends JpaRepository<ExternalLink, UUID> {
    long countByStatus(ExternalLink.LinkStatus status);
}
