package com.github.seregamorph.testsmartcontext.testkit;

import com.github.seregamorph.testsmartcontext.CurrentTestContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class ContextEventTracker implements InitializingBean, DisposableBean {

    @Override
    public void afterPropertiesSet() {
        TestEventTracker.trackEvent("Creating context for " + CurrentTestContext.getCurrentTestClassName());
    }

    @Override
    public void destroy() {
        String currentTestClass = CurrentTestContext.getCurrentTestClassName();
        if (currentTestClass == null) {
            // system shutdown hook
            TestEventTracker.trackEvent("Destroying context (hook)");
        } else {
            // triggered via SmartDirtiesContextTestExecutionListener or spring DirtiesContextTestExecutionListener
            TestEventTracker.trackEvent("Destroying context for " + currentTestClass);
        }
    }
}
