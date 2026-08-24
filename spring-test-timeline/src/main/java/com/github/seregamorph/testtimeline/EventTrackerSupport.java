package com.github.seregamorph.testtimeline;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sergey Chernov
 */
public abstract class EventTrackerSupport {

    private static final Logger logger = LoggerFactory.getLogger(EventTrackerSupport.class);

    /**
     * Both reports are written to the current working directory, which for a Maven or Gradle test
     * execution is the module directory. The HTML report inlines the JSON, but the JSON is kept as
     * a separate machine-readable artifact.
     */
    private static final String JSON_REPORT_FILE_NAME = "spring-test-timeline.json";
    private static final String HTML_REPORT_FILE_NAME = "spring-test-timeline.html";

    /**
     * Number of the currently open (nested) trackings. TestNG executed via the JUnit Platform {@code testng-engine}
     * is tracked both by the JUnit Platform launcher session listener and by the TestNG execution listener, in this
     * case only the outermost pair of the calls is effective.
     */
    private static final AtomicInteger depth = new AtomicInteger();

    private static TimelineHelper timelineHelper;

    protected static void startEventTracking() {
        if (depth.getAndIncrement() > 0) {
            return;
        }
        timelineHelper = new TimelineHelper();
        timelineHelper.start();
    }

    protected static void finishEventTracking() {
        if (depth.decrementAndGet() > 0) {
            return;
        }
        // saves the report
        if (timelineHelper != null) {
            TimelineReportData report = timelineHelper.generateReport();
            File jsonFile = new File(JSON_REPORT_FILE_NAME);
            byte[] reportData = JsonSerializers.serialize(report);
            MoreFileUtils.write(jsonFile, reportData);

            // the HTML report is a diagnostic artifact: a failure to render it should never
            // fail the test run that just succeeded
            try {
                File htmlFile = new File(HTML_REPORT_FILE_NAME);
                MoreFileUtils.write(htmlFile, TimelineHtmlReport.render(reportData));
                logger.info("Spring test timeline report: {}", htmlFile.getAbsolutePath());
            } catch (RuntimeException e) {
                logger.warn("Failed to generate {}, the raw data is in {}", HTML_REPORT_FILE_NAME,
                    jsonFile.getAbsolutePath(), e);
            }
        }
    }

    /*
    @Nullable
    private static File getReportsBaseDir() {
        // todo target
        // "basedir" is provided by Maven, it's module root
        String basedirProperty = System.getProperty("basedir");
        if (basedirProperty == null) {
            return null;
        }

        File basedir = new File(basedirProperty, "leakage-detector");
        if ((basedir.mkdir() || basedir.exists()) && basedir.isDirectory()) {
            return basedir;
        }
        logger.warn("Failed to create {}", basedir);
        return null;
    }
    */

    static void contextCreated(String contextId) {
        if (timelineHelper == null) {
            logger.warn("timelineHelper not initialized");
            return;
        }
        timelineHelper.contextCreated(contextId);
    }

    static void trackEvent(String contextId, ContextEventType eventType) {
        if (timelineHelper == null) {
            logger.warn("timelineHelper not initialized");
            return;
        }
        timelineHelper.addEvent(contextId, eventType);
    }
}
