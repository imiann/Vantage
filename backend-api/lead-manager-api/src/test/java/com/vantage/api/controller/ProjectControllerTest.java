package com.vantage.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantage.api.dto.ProjectRequest;
import com.vantage.api.entity.Project;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/projects should return 201 Created")
    void createProject_ReturnsCreated() throws Exception {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTitle("New Project");

        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(project);

        ProjectRequest request = new ProjectRequest(UUID.randomUUID(), "New Project", null, null, null, null, null);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Project"));
    }

    @Test
    @DisplayName("GET /api/projects should return 200 OK")
    void getAllProjects_ReturnsOk() throws Exception {
        Project project = new Project();
        project.setTitle("Project Alpha");

        when(projectService.getAllProjects()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Project Alpha"));
    }

    @Test
    @DisplayName("GET /api/projects?clientId={id} should return 200 OK")
    void getProjectsByClientId_ReturnsOk() throws Exception {
        UUID clientId = UUID.randomUUID();
        Project project = new Project();
        project.setTitle("Project Alpha");

        when(projectService.getProjectsByClient(clientId)).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects").param("clientId", clientId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Project Alpha"));
    }

    @Test
    @DisplayName("DELETE /api/projects/{id} should return 204 No Content")
    void deleteProject_ReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(projectService).deleteProject(id);

        mockMvc.perform(delete("/api/projects/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/projects/{id} should return 409 Conflict when not UPCOMING")
    void deleteProject_NotUpcoming_ReturnsConflict() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalStateException("Not upcoming")).when(projectService).deleteProject(id);

        mockMvc.perform(delete("/api/projects/{id}", id))
                .andExpect(status().isConflict());
    }
}
