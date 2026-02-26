package com.vantage.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vantage.api.dto.LinkRequest;
import com.vantage.api.entity.ExternalLink;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.service.LinkService;
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

/**
 * Controller-level tests verifying HTTP status codes and error response bodies.
 */
@WebMvcTest(LinkController.class)
public class LinkControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private LinkService linkService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        // --- POST /api/links ---

        @Test
        void postLink_validRequest_returns202() throws Exception {
                ExternalLink saved = new ExternalLink();
                saved.setId(UUID.randomUUID());
                saved.setUrl("https://google.com");
                saved.setName("Google");
                saved.setStatus(ExternalLink.LinkStatus.PENDING);

                when(linkService.createValidationTask(anyString(), any(), anyString()))
                                .thenReturn(saved);

                String body = objectMapper.writeValueAsString(
                                new LinkRequest("https://google.com", null, "Google"));

                mockMvc.perform(post("/api/links")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isAccepted())
                                .andExpect(jsonPath("$.url").value("https://google.com"))
                                .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        void postLink_missingUrl_returns400() throws Exception {
                String body = "{\"name\": \"No URL\"}";

                mockMvc.perform(post("/api/links")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void postLink_invalidUrl_returns400() throws Exception {
                String body = "{\"url\": \"not-a-url\"}";

                mockMvc.perform(post("/api/links")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));
        }

        // --- GET /api/links ---

        @Test
        void getAll_returns200WithList() throws Exception {
                ExternalLink link1 = new ExternalLink();
                link1.setId(UUID.randomUUID());
                link1.setUrl("https://a.com");
                link1.setStatus(ExternalLink.LinkStatus.VALIDATED);

                ExternalLink link2 = new ExternalLink();
                link2.setId(UUID.randomUUID());
                link2.setUrl("https://b.com");
                link2.setStatus(ExternalLink.LinkStatus.BROKEN);

                when(linkService.getAllLinks()).thenReturn(List.of(link1, link2));

                mockMvc.perform(get("/api/links"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].url").value("https://a.com"))
                                .andExpect(jsonPath("$[1].url").value("https://b.com"));
        }

        // --- GET /api/links/{id} ---

        @Test
        void getById_found_returns200() throws Exception {
                UUID id = UUID.randomUUID();
                ExternalLink link = new ExternalLink();
                link.setId(id);
                link.setUrl("https://found.com");
                link.setStatus(ExternalLink.LinkStatus.VALIDATED);

                when(linkService.getLinkById(id)).thenReturn(link);

                mockMvc.perform(get("/api/links/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.url").value("https://found.com"))
                                .andExpect(jsonPath("$.status").value("VALIDATED"));
        }

        @Test
        void getById_notFound_returns404() throws Exception {
                UUID id = UUID.randomUUID();
                when(linkService.getLinkById(id))
                                .thenThrow(new ResourceNotFoundException("ExternalLink", id));

                mockMvc.perform(get("/api/links/{id}", id))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").exists());
        }

        // --- PUT /api/links/{id} ---

        @Test
        void putLink_found_returns200() throws Exception {
                UUID id = UUID.randomUUID();
                ExternalLink updated = new ExternalLink();
                updated.setId(id);
                updated.setUrl("https://updated.com");
                updated.setName("Updated");
                updated.setStatus(ExternalLink.LinkStatus.PENDING);

                when(linkService.updateLink(eq(id), anyString(), any(), anyString()))
                                .thenReturn(updated);

                String body = objectMapper.writeValueAsString(
                                new LinkRequest("https://updated.com", null, "Updated"));

                mockMvc.perform(put("/api/links/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.url").value("https://updated.com"))
                                .andExpect(jsonPath("$.name").value("Updated"));
        }

        @Test
        void putLink_notFound_returns404() throws Exception {
                UUID id = UUID.randomUUID();
                when(linkService.updateLink(eq(id), anyString(), any(), any()))
                                .thenThrow(new ResourceNotFoundException("ExternalLink", id));

                String body = objectMapper.writeValueAsString(
                                new LinkRequest("https://whatever.com", null, null));

                mockMvc.perform(put("/api/links/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void putLink_invalidUrl_returns400() throws Exception {
                UUID id = UUID.randomUUID();
                String body = "{\"url\": \"not-a-url\"}";

                mockMvc.perform(put("/api/links/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));
        }

        // --- DELETE /api/links/{id} ---

        @Test
        void deleteLink_returns204() throws Exception {
                UUID id = UUID.randomUUID();
                doNothing().when(linkService).deleteLink(id);

                mockMvc.perform(delete("/api/links/{id}", id))
                                .andExpect(status().isNoContent());
        }

        @Test
        void deleteLink_notFound_returns404() throws Exception {
                UUID id = UUID.randomUUID();
                doThrow(new ResourceNotFoundException("ExternalLink", id))
                                .when(linkService).deleteLink(id);

                mockMvc.perform(delete("/api/links/{id}", id))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }
}
