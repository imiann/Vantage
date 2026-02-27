package com.vantage.api.service;

import com.vantage.api.dto.ProjectRequest;
import com.vantage.api.entity.Project;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.repository.ClientRepository;
import com.vantage.api.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;

    public ProjectService(ProjectRepository projectRepository, ClientRepository clientRepository) {
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public Project createProject(ProjectRequest request) {
        if (!clientRepository.existsById(request.clientId())) {
            throw new ResourceNotFoundException("Client", request.clientId());
        }
        Project project = new Project();
        project.setClientId(request.clientId());
        mapRequestToProject(request, project);
        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsByClient(UUID clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }
        return projectRepository.findByClientId(clientId);
    }

    public Project getProjectById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    @Transactional
    public Project updateProject(UUID id, ProjectRequest request) {
        Project project = getProjectById(id);
        // Client ID cannot be changed after creation per Architecture.md
        mapRequestToProject(request, project);
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(UUID id) {
        Project project = getProjectById(id);
        if (project.getStatus() != Project.ProjectStatus.UPCOMING) {
            throw new IllegalStateException("Only projects in UPCOMING status can be deleted. Use ARCHIVED for other statuses.");
        }
        projectRepository.deleteById(id);
    }

    @Transactional
    public Project updateStatus(UUID id, Project.ProjectStatus status) {
        Project project = getProjectById(id);
        project.setStatus(status);
        return projectRepository.save(project);
    }

    private void mapRequestToProject(ProjectRequest request, Project project) {
        project.setTitle(request.title());
        project.setDescription(request.description());
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        project.setPrice(request.price());
        if (request.currency() != null) {
            project.setCurrency(request.currency());
        }
        project.setNotes(request.notes());
    }
}
