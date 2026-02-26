package com.vantage.api.metrics;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.ConfigurationParameter;

@Suite
@SelectClasses({
    com.vantage.api.LeadManagerApiApplicationTests.class,
    com.vantage.api.service.LinkServiceTest.class,
    com.vantage.api.service.LinkWorkerServiceTest.class
})
@ConfigurationParameter(key = "junit.platform.launcher.interceptors.enabled", value = "true")
public class TestMetricsSuite {
}
