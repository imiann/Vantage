package com.vantage.api.service;

import com.vantage.api.dto.LinkValidationTask;
import com.vantage.api.entity.ExternalLink;
import com.vantage.api.exception.ResourceNotFoundException;
import com.vantage.api.repository.ExternalLinkRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LinkService {

    private final ExternalLinkRepository repository;
    private final RedisTemplate<String, LinkValidationTask> redisTemplate;

    public LinkService(ExternalLinkRepository repository, RedisTemplate<String, LinkValidationTask> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Saves a new external link and queues it for async validation.
     *
     * @param url       the external URL (required)
     * @param projectId optional FK to the owning project
     * @param name      optional human-readable label
     */
    @Transactional
    public ExternalLink createValidationTask(String url, UUID projectId, String name) {
        ExternalLink link = new ExternalLink();
        link.setUrl(url);
        link.setProjectId(projectId);
        link.setName(name);
        link.setStatus(ExternalLink.LinkStatus.PENDING);
        ExternalLink savedLink = repository.save(link);

        LinkValidationTask task = new LinkValidationTask(savedLink.getId(), url);
        redisTemplate.convertAndSend("link-validation", task);

        return savedLink;
    }

    /** Returns all external links. */
    public List<ExternalLink> getAllLinks() {
        return repository.findAll();
    }

    /**
     * Returns a single link by ID.
     *
     * @throws ResourceNotFoundException if not found
     */
    public ExternalLink getLinkById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExternalLink", id));
    }

    /**
     * Updates an existing link. Re-queues for validation if the URL changed.
     *
     * @throws ResourceNotFoundException if not found
     */

    @Transactional
    public ExternalLink updateLink(UUID id, String newUrl, UUID projectId, String name) {
        ExternalLink link = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExternalLink", id));

        link.setProjectId(projectId);
        link.setName(name);

        if (!link.getUrl().equals(newUrl)) {
            link.setUrl(newUrl);
            link.setStatus(ExternalLink.LinkStatus.PENDING);
            ExternalLink updatedLink = repository.save(link);

            LinkValidationTask task = new LinkValidationTask(updatedLink.getId(), newUrl);
            redisTemplate.convertAndSend("link-validation", task);
            return updatedLink;
        }

        return repository.save(link);
    }

    /** Deletes a link by ID. */
    public void deleteLink(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("ExternalLink", id);
        }
        repository.deleteById(id);
    }

    /** Deletes all links. */
    public void deleteAllLinks() {
        repository.deleteAll();
    }
}
