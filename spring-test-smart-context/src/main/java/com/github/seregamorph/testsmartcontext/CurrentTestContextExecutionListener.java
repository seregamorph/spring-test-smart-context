package com.github.seregamorph.testsmartcontext;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

public class CurrentTestContextExecutionListener implements TestExecutionListener {

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        CurrentTestContext.setCurrentTestClassIdentifier(getTestClassName(testIdentifier));
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        CurrentTestContext.resetCurrentTestClassIdentifier();
    }

    private static String getTestClassName(TestIdentifier testIdentifier) {
        return testIdentifier.getSource()
            .map(CurrentTestContextExecutionListener::getTestClassName)
            .orElse("undefined");
    }

    private static String getTestClassName(TestSource source) {
        // in the common case it can be not the class name
        if (source instanceof ClassSource) {
            ClassSource classSource = (ClassSource) source;
            return classSource.getClassName();
        }
        return source.toString();
    }
}
