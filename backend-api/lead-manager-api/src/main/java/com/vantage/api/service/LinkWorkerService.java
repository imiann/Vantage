package com.vantage.api.service;

import com.vantage.api.dto.LinkValidationTask;
import com.vantage.api.entity.ExternalLink;
import com.vantage.api.entity.ExternalLink.LinkStatus;
import com.vantage.api.repository.ExternalLinkRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class LinkWorkerService {

    private final ExternalLinkRepository repository;
    private final HttpClient httpClient;

    public LinkWorkerService(ExternalLinkRepository repository) {
        this.repository = repository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public void handleMessage(LinkValidationTask task) {
        System.out.println("Processing task for URL: " + task.url());

        try {
            // 1. Perform a HEAD request (efficient check)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(task.url()))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            // 2. Determine status based on HTTP code
            LinkStatus finalStatus = (response.statusCode() >= 200 && response.statusCode() < 400)
                    ? LinkStatus.VALIDATED : LinkStatus.BROKEN;

            // 3. Update the database
            repository.findById(task.id()).ifPresent(link -> {
                link.setStatus(finalStatus);
                repository.save(link);
                System.out.println("Updated link " + task.id() + " to " + finalStatus);
            });

        } catch (Exception e) {
            // If the domain doesn't exist or times out, it's BROKEN
            repository.findById(task.id()).ifPresent(link -> {
                link.setStatus(LinkStatus.BROKEN);
                repository.save(link);
            });
        }
    }
}