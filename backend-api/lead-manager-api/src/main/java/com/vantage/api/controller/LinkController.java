package com.vantage.api.controller;

import com.vantage.api.entity.ExternalLink;
import com.vantage.api.service.LinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/links")
public class LinkController {
    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    // Create
    @PostMapping
    public ResponseEntity<Void> addLink(
            @RequestBody @jakarta.validation.Valid com.vantage.api.dto.LinkRequest request) {
        linkService.createValidationTask(request.url());
        return ResponseEntity.accepted().build();
    }

    // Read all
    @GetMapping
    public List<ExternalLink> getAllLinks() {
        return linkService.getAllLinks();
    }

    // Read one
    @GetMapping("/{id}")
    public Optional<ExternalLink> getLinkById(@PathVariable UUID id) {
        return linkService.getLinkById(id);
    }

    // Update
    @PutMapping("/{id}")
    public ExternalLink updateLink(@PathVariable UUID id,
            @RequestBody @jakarta.validation.Valid com.vantage.api.dto.LinkRequest request) {
        return linkService.updateLink(id, request.url());
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deleteLink(@PathVariable UUID id) {
        linkService.deleteLink(id);
    }
}
