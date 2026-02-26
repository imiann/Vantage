package com.vantage.api.worker;

import com.vantage.api.dto.LinkValidationTask;
import com.vantage.api.entity.ExternalLink.LinkStatus;
import com.vantage.api.repository.ExternalLinkRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class LinkWorkerService {

    private final ExternalLinkRepository repository;
    private final HttpClient httpClient;
    private final Timer validationTimer;

    public LinkWorkerService(ExternalLinkRepository repository,
            HttpClient httpClient,
            MeterRegistry meterRegistry) {
        this.repository = repository;
        this.httpClient = httpClient;
        this.validationTimer = Timer.builder("link.validation.latency")
                .description("Time taken to validate an external link via HEAD request")
                .register(meterRegistry);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public LinkWorkerService(ExternalLinkRepository repository,
            MeterRegistry meterRegistry) {
        this(repository,
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build(),
                meterRegistry);
    }

    @Transactional
    public void handleMessage(LinkValidationTask task) {
        System.out.println("Processing task for URL: " + task.url());

        validationTimer.record(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(task.url()))
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

                LinkStatus finalStatus = (response.statusCode() >= 200 && response.statusCode() < 400)
                        ? LinkStatus.VALIDATED
                        : LinkStatus.BROKEN;

                repository.findById(task.id()).ifPresent(link -> {
                    link.setStatus(finalStatus);
                    link.setLastChecked(LocalDateTime.now());
                    repository.save(link);
                    System.out.println("Updated link " + task.id() + " to " + finalStatus);
                });

            } catch (Exception e) {
                repository.findById(task.id()).ifPresent(link -> {
                    link.setStatus(LinkStatus.BROKEN);
                    link.setLastChecked(LocalDateTime.now());
                    repository.save(link);
                });
            }
        });
    }
}