package com.vantage.api.service;

import com.vantage.api.dto.LinkValidationTask;
import com.vantage.api.entity.ExternalLink;
import com.vantage.api.repository.ExternalLinkRepository;
import com.vantage.api.worker.LinkWorkerService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LinkWorkerServiceTest {

    @Mock
    private ExternalLinkRepository repository;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<Object> httpResponse;

    private MeterRegistry meterRegistry;
    private LinkWorkerService workerService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        workerService = new LinkWorkerService(repository, httpClient, meterRegistry);
    }

    // --- SCENARIO 1: LINK IS VALID ---
    @Test
    void shouldMarkAsValidatedWhenHttpStatusIs200() throws Exception {
        UUID linkId = UUID.randomUUID();
        LinkValidationTask task = new LinkValidationTask(linkId, "https://google.com");
        ExternalLink link = new ExternalLink();
        link.setId(linkId);

        when(repository.findById(linkId)).thenReturn(Optional.of(link));
        lenient().when(httpClient.send(any(), any())).thenReturn(httpResponse);
        lenient().when(httpResponse.statusCode()).thenReturn(200);

        workerService.handleMessage(task);

        verify(repository).save(argThat(l -> l.getStatus() == ExternalLink.LinkStatus.VALIDATED));
    }

    // --- SCENARIO 2: LINK IS BROKEN (404) ---
    @Test
    void shouldMarkAsBrokenWhenHttpStatusIs404() throws Exception {
        UUID linkId = UUID.randomUUID();
        LinkValidationTask task = new LinkValidationTask(linkId, "https://bad-url.com");
        ExternalLink link = new ExternalLink();
        link.setId(linkId);

        when(repository.findById(linkId)).thenReturn(Optional.of(link));
        lenient().when(httpClient.send(any(), any())).thenReturn(httpResponse);
        lenient().when(httpResponse.statusCode()).thenReturn(404);

        workerService.handleMessage(task);

        verify(repository).save(argThat(l -> l.getStatus() == ExternalLink.LinkStatus.BROKEN));
    }

    // --- SCENARIO 3: NETWORK ERROR (TIMEOUT/DNS) ---
    @Test
    void shouldMarkAsBrokenWhenConnectionFails() throws Exception {
        UUID linkId = UUID.randomUUID();
        LinkValidationTask task = new LinkValidationTask(linkId, "https://non-existent-site.test");

        ExternalLink link = new ExternalLink();
        link.setId(linkId);

        when(repository.findById(linkId)).thenReturn(Optional.of(link));
        lenient().when(httpClient.send(any(), any())).thenThrow(new IOException("Connection Refused"));

        workerService.handleMessage(task);

        verify(repository).save(argThat(updatedLink -> updatedLink.getStatus() == ExternalLink.LinkStatus.BROKEN));
    }

    // --- SCENARIO 4: LINK DELETED BEFORE PROCESSING ---
    @Test
    void shouldDoNothingIfLinkNotFoundInDb() {
        UUID linkId = UUID.randomUUID();
        LinkValidationTask task = new LinkValidationTask(linkId, "https://github.com");

        when(repository.findById(linkId)).thenReturn(Optional.empty());

        workerService.handleMessage(task);

        verify(repository, never()).save(any());
    }
}
