package com.vantage.api.controller;

import com.vantage.api.entity.ExternalLink;
import com.vantage.api.service.LinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/links")
public class LinkController {
    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    // Create
    @PostMapping
    public void addLink(@RequestBody String url) {
        linkService.createValidationTask(url);
    }

    // Read all
    @GetMapping
    public List<ExternalLink> getAllLinks() {
        return linkService.getAllLinks();
    }

    // Read one
    @GetMapping("/{id}")
    public Optional<ExternalLink> getLinkById(@PathVariable Long id) {
        return linkService.getLinkById(id);
    }

    // Update
    @PutMapping("/{id}")
    public ExternalLink updateLink(@PathVariable Long id, @RequestBody String newUrl) {
        return linkService.updateLink(id, newUrl);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void deleteLink(@PathVariable Long id) {
        linkService.deleteLink(id);
    }
}
