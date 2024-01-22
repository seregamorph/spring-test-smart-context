package com.github.seregamorph.testsmartcontext.testng;

import com.github.seregamorph.testsmartcontext.CurrentTestContext;
import org.testng.IClassListener;
import org.testng.ITestClass;

public class CurrentTestListener extends CurrentTestContext implements IClassListener {

    @Override
    public void onBeforeClass(ITestClass testClass) {
        Class<?> realTestClass = testClass.getRealClass();
        setCurrentTestClassIdentifier(realTestClass.getName());
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        resetCurrentTestClassIdentifier();
    }
}
