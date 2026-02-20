package com.vantage.api.controller;

import com.vantage.api.entity.ExternalLink;
import com.vantage.api.service.LinkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/links")
public class LinkController {
    private final LinkService leadService;

    public LinkController(LinkService leadService) {
        this.leadService = leadService;
    }

    @PostMapping
    public void addLink(@RequestBody String url) {
        leadService.createValidationTask(url);
    }

    @GetMapping
    public List<ExternalLink> getAllLinks() {
        return leadService.getAllLinks();
    }
}
