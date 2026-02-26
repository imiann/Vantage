package com.vantage.api.service;

import com.vantage.api.dto.LinkValidationTask;
import com.vantage.api.entity.ExternalLink;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.repository.ExternalLinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {

    @Mock
    private ExternalLinkRepository repository;

    @Mock
    private RedisTemplate<String, LinkValidationTask> redisTemplate;

    @InjectMocks
    private LinkService linkService;

    // --- CREATE ---
    @Test
    void shouldSaveLinkAndSendMessage() {
        String url = "https://test.com";
        UUID projectId = UUID.randomUUID();
        String name = "Test Link";

        ExternalLink savedLink = new ExternalLink();
        savedLink.setId(UUID.randomUUID());
        savedLink.setUrl(url);
        savedLink.setProjectId(projectId);
        savedLink.setName(name);

        when(repository.save(any(ExternalLink.class))).thenReturn(savedLink);

        ExternalLink result = linkService.createValidationTask(url, projectId, name);

        assertNotNull(result);
        assertEquals(url, result.getUrl());
        assertEquals(projectId, result.getProjectId());
        assertEquals(name, result.getName());
        verify(repository, times(1)).save(any(ExternalLink.class));
        verify(redisTemplate, times(1)).convertAndSend(eq("link-validation"), any(LinkValidationTask.class));
    }

    @Test
    void shouldSaveLinkWithoutProjectIdOrName() {
        String url = "https://standalone.com";
        ExternalLink savedLink = new ExternalLink();
        savedLink.setId(UUID.randomUUID());
        savedLink.setUrl(url);

        when(repository.save(any(ExternalLink.class))).thenReturn(savedLink);

        ExternalLink result = linkService.createValidationTask(url, null, null);

        assertNotNull(result);
        assertNull(result.getProjectId());
        assertNull(result.getName());
        verify(repository).save(any(ExternalLink.class));
    }

    // --- READ ---
    @Test
    void shouldReturnAllLinks() {
        when(repository.findAll()).thenReturn(List.of(new ExternalLink(), new ExternalLink()));

        List<ExternalLink> results = linkService.getAllLinks();

        assertEquals(2, results.size());
        verify(repository).findAll();
    }

    @Test
    void shouldReturnLinkById() {
        UUID id = UUID.randomUUID();
        ExternalLink link = new ExternalLink();
        link.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(link));

        ExternalLink result = linkService.getLinkById(id);

        assertEquals(id, result.getId());
        verify(repository).findById(id);
    }

    @Test
    void shouldThrowWhenLinkNotFoundById() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> linkService.getLinkById(id));
    }

    // --- UPDATE (URL CHANGED) ---
    @Test
    void shouldUpdateUrlAndTriggerNewValidation() {
        UUID id = UUID.randomUUID();
        String oldUrl = "https://old.com";
        String newUrl = "https://new.com";
        UUID projectId = UUID.randomUUID();
        String name = "Updated Link";

        ExternalLink existingLink = new ExternalLink();
        existingLink.setId(id);
        existingLink.setUrl(oldUrl);

        when(repository.findById(id)).thenReturn(Optional.of(existingLink));
        when(repository.save(any(ExternalLink.class))).thenAnswer(i -> i.getArguments()[0]);

        linkService.updateLink(id, newUrl, projectId, name);

        verify(repository).save(argThat(link -> link.getUrl().equals(newUrl)
                && link.getStatus() == ExternalLink.LinkStatus.PENDING
                && link.getName().equals(name)));
        verify(redisTemplate).convertAndSend(eq("link-validation"), any(LinkValidationTask.class));
    }

    // --- UPDATE (URL SAME) ---
    @Test
    void shouldUpdateNameAndProjectIdEvenIfUrlIsSame() {
        UUID id = UUID.randomUUID();
        String url = "https://same.com";
        UUID projectId = UUID.randomUUID();
        String name = "New Name";

        ExternalLink existingLink = new ExternalLink();
        existingLink.setId(id);
        existingLink.setUrl(url);

        when(repository.findById(id)).thenReturn(Optional.of(existingLink));
        when(repository.save(any(ExternalLink.class))).thenAnswer(i -> i.getArguments()[0]);

        linkService.updateLink(id, url, projectId, name);

        // Should save with updated name and projectId but NOT send to Redis
        verify(repository).save(argThat(link -> link.getName().equals(name) && link.getProjectId().equals(projectId)));
        verify(redisTemplate, never()).convertAndSend(anyString(), any(LinkValidationTask.class));
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentLink() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> linkService.updateLink(id, "https://whatever.com", null, null));
    }

    // --- DELETE ---
    @Test
    void shouldDeleteLink() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        linkService.deleteLink(id);

        verify(repository).deleteById(id);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentLink() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> linkService.deleteLink(id));
    }
}
