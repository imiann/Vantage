package com.vantage.api.controller;

import com.vantage.api.dto.LinkRequest;
import com.vantage.api.entity.ExternalLink;
import com.vantage.api.service.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/links")
@Tag(name = "Links", description = "External link management and async validation")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @Operation(summary = "Queue a new link for async validation")
    @PostMapping
    public ResponseEntity<ExternalLink> addLink(@RequestBody @Valid LinkRequest request) {
        ExternalLink created = linkService.createValidationTask(
                request.url(), request.projectId(), request.name());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(created);
    }

    @Operation(summary = "List all external links")
    @GetMapping
    public ResponseEntity<List<ExternalLink>> getAllLinks() {
        return ResponseEntity.ok(linkService.getAllLinks());
    }

    @Operation(summary = "Get a single link by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ExternalLink> getLinkById(@PathVariable UUID id) {
        return ResponseEntity.ok(linkService.getLinkById(id));
    }

    @Operation(summary = "Update an existing link")
    @PutMapping("/{id}")
    public ResponseEntity<ExternalLink> updateLink(@PathVariable UUID id,
            @RequestBody @Valid LinkRequest request) {
        ExternalLink updated = linkService.updateLink(
                id, request.url(), request.projectId(), request.name());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a link by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLink(@PathVariable UUID id) {
        linkService.deleteLink(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all links (development use only)")
    @DeleteMapping("/DELETEALLCONFIRM")
    public ResponseEntity<Void> deleteAllLinks() {
        linkService.deleteAllLinks();
        return ResponseEntity.noContent().build();
    }
}
