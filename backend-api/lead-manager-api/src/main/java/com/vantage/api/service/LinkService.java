package com.vantage.api.service;

import com.vantage.api.dto.LinkValidationTask;
import com.vantage.api.entity.ExternalLink;
import com.vantage.api.repository.ExternalLinkRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LinkService {

    private final ExternalLinkRepository repository;
    private final RedisTemplate<String, LinkValidationTask> redisTemplate;

    public LinkService(ExternalLinkRepository repository, RedisTemplate<String, LinkValidationTask> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional // If database save fails, process stops.
    public void createValidationTask(String url) {
        // 1. Save to Database
        ExternalLink link = new ExternalLink();
        link.setUrl(url);
        link.setStatus(ExternalLink.LinkStatus.PENDING);
        ExternalLink savedLink = repository.save(link);

        // 2. Wrap in DTO
        LinkValidationTask task = new LinkValidationTask(savedLink.getId(), url);

        // 3. Publish to Redis queue
        redisTemplate.convertAndSend("link-validation", task);
    }

    public List<ExternalLink> getAllLinks() {
        return repository.findAll();
    }

    public Optional<ExternalLink> getLinkById(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    public ExternalLink updateLink(UUID id, String newUrl) {
        return repository.findById(id).map(link -> {

            // Different url
            if (!link.getUrl().equals(newUrl)) {
                link.setUrl(newUrl);
                link.setStatus(ExternalLink.LinkStatus.PENDING);
                ExternalLink updatedLink = repository.save(link);

                LinkValidationTask task = new LinkValidationTask(updatedLink.getId(), newUrl);
                redisTemplate.convertAndSend("link-validation", task);
                return updatedLink;
            }
            // Same url, nothing changed
            return link;
        }).orElseThrow(() -> new RuntimeException("Link not found with id " + id));
    }

    public void deleteLink(UUID id) {
        repository.deleteById(id);
    }

    public void deleteAllLinks() {
        repository.deleteAll();
    }
}
