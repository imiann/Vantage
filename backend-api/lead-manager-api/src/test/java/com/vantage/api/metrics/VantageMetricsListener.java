package com.vantage.api.metrics;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.engine.TestExecutionResult;

import java.util.concurrent.atomic.AtomicInteger;

public class VantageMetricsListener implements TestExecutionListener {
    private final AtomicInteger total = new AtomicInteger(0);
    private final AtomicInteger succeeded = new AtomicInteger(0);
    private final AtomicInteger failed = new AtomicInteger(0);
    private final AtomicInteger skipped = new AtomicInteger(0);
    private long startTime;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        startTime = System.currentTimeMillis();
        System.out.println("\n>>> Vantage Metrics Collector Started...");
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (testIdentifier.isTest()) {
            skipped.incrementAndGet();
        }
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            total.incrementAndGet();
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            if (testExecutionResult.getStatus() == TestExecutionResult.Status.SUCCESSFUL) {
                succeeded.incrementAndGet();
            } else {
                failed.incrementAndGet();
            }
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("\n================================================");
        System.out.println("           VANTAGE TEST SUITE METRICS          ");
        System.out.println("================================================\n");
        
        System.out.printf("Tests Started:        %d%n", total.get());
        System.out.printf("Tests Succeeded:      %d%n", succeeded.get());
        System.out.printf("Tests Failed:         %d%n", failed.get());
        System.out.printf("Tests Skipped:        %d%n", skipped.get());
        
        System.out.println("\n--- Performance ---");
        System.out.printf("Total Execution Time: %d ms (%.2f seconds)%n", 
                duration, duration / 1000.0);
        
        if (total.get() > 0) {
            double successRate = (double) succeeded.get() / total.get() * 100;
            System.out.printf("Success Rate:         %.2f%%%n", successRate);
        }
        
        System.out.println("\n================================================\n");
    }
}
