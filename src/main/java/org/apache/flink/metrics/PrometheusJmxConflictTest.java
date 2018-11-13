package org.apache.flink.metrics;

import org.apache.flink.api.common.JobID;
import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.jmx.JMXReporter;
import org.apache.flink.metrics.prometheus.PrometheusReporter;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.apache.flink.runtime.metrics.MetricRegistryConfiguration;
import org.apache.flink.runtime.metrics.MetricRegistryImpl;
import org.apache.flink.runtime.metrics.groups.FrontMetricGroup;
import org.apache.flink.runtime.metrics.groups.TaskManagerJobMetricGroup;
import org.apache.flink.runtime.metrics.groups.TaskManagerMetricGroup;

public class PrometheusJmxConflictTest {

  public static void main(String[] args) {
    Configuration cfg = new Configuration();
    cfg.setString(ConfigConstants.METRICS_REPORTERS_LIST, "prom,jmx");
    cfg.setString(ConfigConstants.METRICS_REPORTER_PREFIX + "prom." + ConfigConstants.METRICS_REPORTER_CLASS_SUFFIX, PrometheusReporter.class.getName());
    cfg.setString(ConfigConstants.METRICS_REPORTER_PREFIX + "prom.port", "45646");

    cfg.setString(ConfigConstants.METRICS_REPORTER_PREFIX + "jmx." + ConfigConstants.METRICS_REPORTER_CLASS_SUFFIX, JMXReporter.class.getName());
    cfg.setString(ConfigConstants.METRICS_REPORTER_PREFIX + "jmx.port", "45647");

    MetricRegistryImpl registry = new MetricRegistryImpl(MetricRegistryConfiguration.fromConfiguration(cfg));

    TaskManagerMetricGroup tmMetricGroup = new TaskManagerMetricGroup(registry, "host", "tm");
    FrontMetricGroup<TaskManagerJobMetricGroup> metricGroup1 = new FrontMetricGroup<>(0, new TaskManagerJobMetricGroup(registry, tmMetricGroup, JobID
        .generate(), "job_1"));
    Counter metric1 = new SimpleCounter();

    for(MetricReporter reporter: registry.getReporters()) {
      reporter.notifyOfAddedMetric(metric1, "test", metricGroup1);
    }
  }
}
