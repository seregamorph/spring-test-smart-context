package com.github.seregamorph.testsmartcontext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpringContextEventLoggerListenerTest {

    @Test
    public void shouldFormatNanos() {
        Assertions.assertEquals("120.000s",
            SpringContextEventLoggerListener.formatNanos(120000000000L));
    }
}
