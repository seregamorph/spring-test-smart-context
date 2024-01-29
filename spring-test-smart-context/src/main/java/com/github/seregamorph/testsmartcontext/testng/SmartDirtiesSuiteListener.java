package com.github.seregamorph.testsmartcontext.testng;

import com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener;
import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsHolder;
import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter;
import java.util.List;
import java.util.Map;
import org.testng.IAlterSuiteListener;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.internal.RuntimeBehavior;
import org.testng.xml.XmlSuite;

/**
 * See description in {@link SmartDirtiesContextTestExecutionListener}.
 * <p>
 * Reorders TestNG test classes in suite grouping ITs with the same context configuration to minimize number of parallel
 * existing contexts.
 * <p>
 * For Jupiter test classes - see {@link com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer}, for
 * JUnit 4 test classes - see {@link com.github.seregamorph.testsmartcontext.SmartDirtiesPostDiscoveryFilter}.
 */
@SuppressWarnings("CodeBlock2Expr")
public class SmartDirtiesSuiteListener extends SmartDirtiesTestsHolder
    implements IAlterSuiteListener, IMethodInterceptor {

    @Override
    public void alter(List<XmlSuite> suites) {
        if (RuntimeBehavior.isDryRun()) {
            // the list of test classes is wrong, listener is called per each class as single in suite
            return;
        }
        // TestNG needs it (otherwise reorders back to default alphabetical order)
        suites.forEach(suite -> {
            suite.getTests().forEach(xmlTest -> {
                xmlTest.setPreserveOrder(false);
            });
        });
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        if (RuntimeBehavior.isDryRun()) {
            // the list of test classes is wrong, listener is called per each class as single in suite
            return methods;
        }

        // this intercept method is executed by TestNG before running the suite (both IDEA and maven)
        SmartDirtiesTestsSorter sorter = SmartDirtiesTestsSorter.getInstance();
        Map<Class<?>, Boolean> lastClassPerConfig = sorter.sort(methods, methodInstance -> {
            return methodInstance.getMethod().getTestClass().getRealClass();
        });

        SmartDirtiesTestsHolder.setLastClassPerConfig(lastClassPerConfig);

        return methods;
    }
}
