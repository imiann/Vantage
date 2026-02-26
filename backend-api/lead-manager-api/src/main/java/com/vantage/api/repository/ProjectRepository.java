package com.vantage.api.repository;

import com.vantage.api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    long countByStatus(Project.ProjectStatus status);
}
