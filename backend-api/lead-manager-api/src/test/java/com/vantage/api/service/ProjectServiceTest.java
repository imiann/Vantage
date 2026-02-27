package com.vantage.api.service;

import com.vantage.api.dto.ProjectRequest;
import com.vantage.api.entity.Project;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.repository.ClientRepository;
import com.vantage.api.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;
    private UUID projectId;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        testProject = new Project();
        testProject.setId(projectId);
        testProject.setClientId(clientId);
        testProject.setTitle("Test Project");
        testProject.setStatus(Project.ProjectStatus.UPCOMING);
    }

    @Test
    @DisplayName("Should create project successfully")
    void createProject_Success() {
        ProjectRequest request = new ProjectRequest(clientId, "Test Project", null, null, null, null, null);
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project created = projectService.createProject(request);

        assertNotNull(created);
        assertEquals("Test Project", created.getTitle());
    }

    @Test
    @DisplayName("Should throw exception when creating project for non-existent client")
    void createProject_NonExistentClient_ThrowsException() {
        ProjectRequest request = new ProjectRequest(clientId, "Test Project", null, null, null, null, null);
        when(clientRepository.existsById(clientId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> projectService.createProject(request));
    }

    @Test
    @DisplayName("Should delete project if status is UPCOMING")
    void deleteProject_Upcoming_Success() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        projectService.deleteProject(projectId);

        verify(projectRepository, times(1)).deleteById(projectId);
    }

    @Test
    @DisplayName("Should throw exception when deleting project not in UPCOMING status")
    void deleteProject_Active_ThrowsException() {
        testProject.setStatus(Project.ProjectStatus.ACTIVE);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        assertThrows(IllegalStateException.class, () -> projectService.deleteProject(projectId));
    }

    @Test
    @DisplayName("Should update status successfully")
    void updateStatus_Success() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project updated = projectService.updateStatus(projectId, Project.ProjectStatus.ACTIVE);

        assertEquals(Project.ProjectStatus.ACTIVE, updated.getStatus());
    }
}
