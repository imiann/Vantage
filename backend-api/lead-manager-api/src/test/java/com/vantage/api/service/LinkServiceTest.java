package com.vantage.api.service;

import com.vantage.api.dto.LinkValidationTask;
import com.vantage.api.entity.ExternalLink;
import com.vantage.api.repository.ExternalLinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;

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
        ExternalLink savedLink = new ExternalLink();
        savedLink.setId(100L);
        savedLink.setUrl(url);

        when(repository.save(any(ExternalLink.class))).thenReturn(savedLink);

        linkService.createValidationTask(url);

        verify(repository, times(1)).save(any(ExternalLink.class));
        verify(redisTemplate, times(1)).convertAndSend(eq("link-validation"), any(LinkValidationTask.class));
    }

    // --- READ ---
    @Test
    void shouldReturnAllLinks() {
        when(repository.findAll()).thenReturn(List.of(new ExternalLink(), new ExternalLink()));

        List<ExternalLink> results = linkService.getAllLinks();

        assert(results.size() == 2);
        verify(repository).findAll();
    }

    // --- UPDATE (URL CHANGED) ---
    @Test
    void shouldUpdateUrlAndTriggerNewValidation() {
        Long id = 1L;
        String oldUrl = "https://old.com";
        String newUrl = "https://new.com";

        ExternalLink existingLink = new ExternalLink();
        existingLink.setId(id);
        existingLink.setUrl(oldUrl);

        when(repository.findById(id)).thenReturn(Optional.of(existingLink));
        when(repository.save(any(ExternalLink.class))).thenAnswer(i -> i.getArguments()[0]);

        linkService.updateLink(id, newUrl);

        // Verify it reset to PENDING and saved
        verify(repository).save(argThat(link -> link.getUrl().equals(newUrl) && link.getStatus() == ExternalLink.LinkStatus.PENDING));
        // Verify it sent a new message to Redis
        verify(redisTemplate).convertAndSend(eq("link-validation"), any(LinkValidationTask.class));
    }

    // --- UPDATE (URL SAME) ---
    @Test
    void shouldNotTriggerRedisIfUrlIsSame() {
        Long id = 1L;
        String url = "https://same.com";
        ExternalLink existingLink = new ExternalLink();
        existingLink.setId(id);
        existingLink.setUrl(url);

        when(repository.findById(id)).thenReturn(Optional.of(existingLink));

        linkService.updateLink(id, url);

        // Should NOT call save or send to Redis
        verify(repository, never()).save(any());
        verify(redisTemplate, never()).convertAndSend(anyString(), any(LinkValidationTask.class));
    }

    // --- DELETE ---
    @Test
    void shouldDeleteLink() {
        Long id = 1L;

        linkService.deleteLink(id);

        verify(repository).deleteById(id);
    }
}
