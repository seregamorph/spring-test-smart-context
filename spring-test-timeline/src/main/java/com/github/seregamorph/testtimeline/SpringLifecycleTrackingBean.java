package com.github.seregamorph.testtimeline;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Sergey Chernov
 */
public class SpringLifecycleTrackingBean implements InitializingBean, DisposableBean {

    private final String contextId;

    public SpringLifecycleTrackingBean(String contextId) {
        this.contextId = contextId;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        onContextCreating();
    }

    @Override
    public void destroy() {
        onContextDestroying();
    }

    private void onContextCreating() {
        EventTrackerSupport.trackEvent(contextId, ContextEventType.CREATING);
    }

    private void onContextDestroying() {
        EventTrackerSupport.trackEvent(contextId, ContextEventType.DESTROYING);
    }
}
