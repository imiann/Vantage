package com.vantage.api.controller;

import com.vantage.api.entity.ExternalLink;
import com.vantage.api.service.LinkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/links")
public class LinkController {
    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping
    public void addLink(@RequestBody String url) {
        linkService.createValidationTask(url);
    }

    @GetMapping
    public List<ExternalLink> getAllLinks() {
        return linkService.getAllLinks();
    }
}
