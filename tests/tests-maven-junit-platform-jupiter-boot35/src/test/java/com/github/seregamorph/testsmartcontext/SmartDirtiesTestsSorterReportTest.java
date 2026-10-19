package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.seregamorph.testsmartcontext.jupiter.JupiterIntegrationTestFilter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Validates the report of {@link SmartDirtiesTestsSorter} printed to stdout. Static member classes are fixtures
 * which are only used to calculate MergedContextConfiguration, they are not executed.
 */
public class SmartDirtiesTestsSorterReportTest {

    @Test
    public void shouldPrintNestedGroupsUnderEnclosingClass() {
        var testItems = new ArrayList<Class<?>>(List.of(
            UnconfiguredOuter.class,
            SharedWithNested.class,
            Outer.class,
            AnotherWithOuterConfig.class
        ));

        String report = captureReport(() -> SmartDirtiesTestsSorter.getInstance().sort(testItems,
            TestClassExtractor.ofClass(testClass -> testClass), JupiterIntegrationTestFilter.getInstance()));

        String prefix = SmartDirtiesTestsSorterReportTest.class.getName();
        assertEquals(String.join("\n",
            "Running suite of 4 integration test classes grouped and reordered by their MergedContextConfiguration"
                + " (4 groups):",
            "---",
            prefix + "$AnotherWithOuterConfig (creates context)",
            prefix + "$Outer (closes context after its nested classes)",
            "    ---",
            "    " + prefix + "$Outer$ExclusiveConfig (creates and closes context)",
            "---",
            prefix + "$Outer$SharedConfig (creates context)",
            prefix + "$SharedWithNested (closes context)",
            "---",
            prefix + "$UnconfiguredOuter$Inner (creates and closes context)"
        ), report);
    }

    private static String captureReport(Runnable runnable) {
        PrintStream prevOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        try {
            runnable.run();
        } finally {
            System.setOut(prevOut);
        }
        String output = out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        prevOut.print(output);
        // the output may also contain Spring log messages
        int reportStart = output.indexOf("Running suite of ");
        assertTrue(reportStart >= 0, "Report not found in output:\n" + output);
        return output.substring(reportStart).stripTrailing();
    }

    public static class ConfigA {
    }

    public static class ConfigB {
    }

    public static class ConfigC {
    }

    public static class ConfigD {
    }

    @SpringJUnitConfig(ConfigA.class)
    public static class AnotherWithOuterConfig {
    }

    @SpringJUnitConfig(ConfigA.class)
    public static class Outer {

        // own context which is not shared with other classes
        @Nested
        @ContextConfiguration(classes = ConfigB.class)
        public class ExclusiveConfig {
        }

        // same context as the enclosing class, not reported
        @Nested
        public class InheritedConfig {
        }

        // context shared with top-level SharedWithNested
        @Nested
        @ContextConfiguration(classes = ConfigC.class)
        public class SharedConfig {
        }
    }

    @SpringJUnitConfig({ConfigA.class, ConfigC.class})
    public static class SharedWithNested {
    }

    // container without own context
    public static class UnconfiguredOuter {

        @Nested
        @SpringJUnitConfig(ConfigD.class)
        public class Inner {
        }
    }
}
