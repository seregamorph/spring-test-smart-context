package com.github.seregamorph.testsmartcontext.leakage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author Sergey Chernov
 */
public class ResourceLeakageCsvLogWriter extends ResourceLeakageLogWriter {

    private final PrintWriter out;
    private final List<String> headers;

    public ResourceLeakageCsvLogWriter(File outputFile, List<String> headers) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile, false);
            out = new PrintWriter(new OutputStreamWriter(fileOutputStream, UTF_8), true);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }

        this.headers = new ArrayList<>(headers);
        StringBuilder fileHeader = new StringBuilder("Timestamp,testClass,event,testGroup,test");
        for (String header : headers) {
            fileHeader.append(",").append(header);
        }
        out.println(fileHeader);
    }

    @Override
    public void write(
        Map<String, Long> indicators,
        Class<?> testClass,
        String event,
        int testGroupNumber,
        int testNumber
    ) {
        String timestamp = getTimestamp();
        StringBuilder line = new StringBuilder(timestamp)
            .append(",").append(testClass.getSimpleName())
            .append(",").append(event)
            .append(",").append(testGroupNumber)
            .append(",").append(testNumber);
        for (String header : headers) {
            Long value = indicators.get(header);
            if (value == null) {
                line.append(",");
            } else {
                line.append(",").append(value);
            }
        }
        out.println(line);
    }

    @Override
    public void close() {
        out.close();
    }
}
