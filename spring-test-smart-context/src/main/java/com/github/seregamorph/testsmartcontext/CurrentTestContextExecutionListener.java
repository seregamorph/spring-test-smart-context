package com.github.seregamorph.testsmartcontext;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

public class CurrentTestContextExecutionListener implements TestExecutionListener {

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        CurrentTestContext.setCurrentTestClassIdentifier(testIdentifier.getSource()
            .map(Object::toString).orElse("undefined"));
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        CurrentTestContext.resetCurrentTestClassIdentifier();
    }
}
