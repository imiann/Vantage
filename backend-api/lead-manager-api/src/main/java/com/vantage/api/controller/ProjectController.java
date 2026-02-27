package com.vantage.api.controller;

import com.vantage.api.dto.ProjectRequest;
import com.vantage.api.entity.Project;
import com.vantage.api.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Create a new project for a client")
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody @Valid ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @Operation(summary = "List all projects, optionally filtered by client ID")
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects(@RequestParam(required = false) Optional<UUID> clientId) {
        return clientId.map(id -> ResponseEntity.ok(projectService.getProjectsByClient(id)))
                .orElseGet(() -> ResponseEntity.ok(projectService.getAllProjects()));
    }

    @Operation(summary = "Get a single project by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @Operation(summary = "Update an existing project by ID")
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable UUID id, @RequestBody @Valid ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @Operation(summary = "Delete a project by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update the status of a project")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Project> updateStatus(@PathVariable UUID id, @RequestBody Project.ProjectStatus status) {
        return ResponseEntity.ok(projectService.updateStatus(id, status));
    }
}
