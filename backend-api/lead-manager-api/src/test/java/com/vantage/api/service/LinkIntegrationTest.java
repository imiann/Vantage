package com.vantage.api.service;
import com.vantage.api.entity.ExternalLink;
import com.vantage.api.repository.ExternalLinkRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class LinkIntegrationTest {

    @Autowired
    private LinkService linkService;

    @Autowired
    private ExternalLinkRepository repository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2");

    // Starts a real Redis container using the official image
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    // Maps the dynamic Docker port to Spring properties
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        redisMessageListenerContainer.stop();
        redisMessageListenerContainer.getConnectionFactory().getConnection().serverCommands().flushDb();
        repository.deleteAll();
        redisMessageListenerContainer.start();
    }

    @AfterEach
    void tearDown() {
        redisMessageListenerContainer.stop();
    }

    @Test
    void shouldHandleManyConcurrentCreateRequests() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final String url = "https://vantage-test-" + i + ".com";
            executor.submit(() -> {
                try {
                    linkService.createValidationTask(url);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Wait for all 50 threads to finish

        assertEquals(threadCount, repository.count()); // Verify DB persistence
    }

    @Test
    void shouldHandleConcurrentUpdatesToSameLink() throws InterruptedException {
        ExternalLink link = new ExternalLink();
        link.setUrl("https://start.com");
        link.setStatus(ExternalLink.LinkStatus.VALIDATED);
        link = repository.save(link);

        Long id = link.getId();
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final String newUrl = "https://update-" + i + ".com";
            executor.submit(() -> {
                try {
                    linkService.updateLink(id, newUrl);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        ExternalLink finalLink = repository.findById(id).orElseThrow();
        assertEquals(ExternalLink.LinkStatus.PENDING, finalLink.getStatus()); // Verify state reset
    }

    @Test
    void shouldHandleMixedWorkingAndBrokenLinks() throws InterruptedException {
        int count = 20;
        List<String> urls = new ArrayList<>();
        // Generate half working, half non-existent URLs
        for (int i = 0; i < count; i++) {
            urls.add(i % 2 == 0 ? "https://google.com" : "https://this-site-does-not-exist.test");
        }

        CountDownLatch latch = new CountDownLatch(count);
        for (String url : urls) {
            new Thread(() -> {
                try {
                    linkService.createValidationTask(url);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();

        // Verify all records were created
        assertEquals(count, repository.count());
    }

    @Test
    void shouldHandleMassiveScale_5000Links() throws InterruptedException {
        int totalLinks = 5000;

        Awaitility.await().until(() -> repository.count() == 0L);

        // The 'try' block ensures the executor closes and waits for tasks
        try (ExecutorService executor = Executors.newFixedThreadPool(50)) {
            for (int i = 0; i < totalLinks; i++) {
                final String url = "https://vantage-scale-test-" + i + ".com";
                executor.submit(() -> linkService.createValidationTask(url));
            }
        } // Shutdown and awaitTermination happen automatically here

        // Wait for the separate Worker threads to finish writing to the DB
        Awaitility.await()
                .atMost(Duration.ofMinutes(5))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> repository.countByStatus(ExternalLink.LinkStatus.PENDING) == 0);

        // Give the context a one-second buffer to finish background cleanup
        Thread.sleep(2000);

        assertEquals(totalLinks, repository.count());
    }
}
