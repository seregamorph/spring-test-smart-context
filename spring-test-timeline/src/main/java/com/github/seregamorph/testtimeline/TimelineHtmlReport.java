package com.github.seregamorph.testtimeline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Renders the single-file HTML timeline report: the {@code timeline-report.html} template shipped
 * as a resource next to this class, with the JSON report inlined in place of
 * {@link #DATA_PLACEHOLDER}.
 *
 * @author Sergey Chernov
 */
final class TimelineHtmlReport {

    private static final String TEMPLATE_RESOURCE = "timeline-report.html";

    /**
     * The token the template carries inside its {@code <script id="timelineData">} element.
     */
    private static final String DATA_PLACEHOLDER = "__SPRING_TEST_TIMELINE_JSON__";

    /**
     * Renders the report with {@code reportJson} inlined.
     *
     * @param reportJson the serialized {@link TimelineReportData}, UTF-8 encoded
     * @return the UTF-8 encoded HTML document
     */
    static byte[] render(byte[] reportJson) {
        String template = readTemplate();
        int pos = template.indexOf(DATA_PLACEHOLDER);
        if (pos < 0) {
            throw new IllegalStateException("Placeholder " + DATA_PLACEHOLDER
                + " not found in " + TEMPLATE_RESOURCE);
        }
        String json = escapeForInlineScript(new String(reportJson, StandardCharsets.UTF_8).trim());
        String html = template.substring(0, pos) + json + template.substring(pos + DATA_PLACEHOLDER.length());
        return html.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * The HTML parser ends a {@code <script>} element at the first {@code </} of a closing tag,
     * regardless of the JSON syntax around it. In JSON a {@code </} can only occur inside a string
     * literal, where {@code \/} is a valid escape for {@code /} - so escaping every {@code </} keeps
     * the document well-formed without changing the parsed value.
     */
    private static String escapeForInlineScript(String json) {
        return json.replace("</", "<\\/");
    }

    private static String readTemplate() {
        try (InputStream in = TimelineHtmlReport.class.getResourceAsStream(TEMPLATE_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found: " + TEMPLATE_RESOURCE);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TimelineHtmlReport() {
    }
}