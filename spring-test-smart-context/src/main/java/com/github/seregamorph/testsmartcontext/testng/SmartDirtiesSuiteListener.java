package com.github.seregamorph.testsmartcontext.testng;

import com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener;
import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter;
import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSupport;
import com.github.seregamorph.testsmartcontext.TestClassExtractor;
import com.github.seregamorph.testsmartcontext.TestSortResult;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
 *
 * @author Sergey Chernov
 */
@SuppressWarnings("CodeBlock2Expr")
public class SmartDirtiesSuiteListener extends SmartDirtiesTestsSupport
    implements IAlterSuiteListener, IMethodInterceptor {

    @Override
    public void alter(List<XmlSuite> suites) {
        // dryRun is only true when called via junit5 testng-engine on discovery phase, there will be subsequent
        // call of this method with dryRun=false on execution phase
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
        // dryRun is only true when called via junit5 testng-engine on discovery phase, there will be subsequent
        // call of this method with dryRun=false on execution phase
        if (RuntimeBehavior.isDryRun()) {
            // the list of test classes is wrong, listener is called per each class as single in suite
            return methods;
        }

        Set<Class<?>> uniqueClasses = new LinkedHashSet<>();
        for (IMethodInstance method : methods) {
            Class<?> testClass = getTestClass(method);
            uniqueClasses.add(testClass);
        }

        if (uniqueClasses.size() == 1) {
            Class<?> testClass = getTestClass(methods.get(0));
            SmartDirtiesTestsSupport.setTestClassesLists(ENGINE_TESTNG, TestSortResult.singletonList(testClass));
            return methods;
        }

        // this intercept method is executed by TestNG before running the suite (both IDEA and maven)
        SmartDirtiesTestsSorter sorter = SmartDirtiesTestsSorter.getInstance();
        // Do not store the failure as if it throws, TestNG will fail the suite
        // (both pure TestNG and JUnit testng-engine)
        TestSortResult testClassesLists = sorter.sort(methods,
            TestClassExtractor.ofMethod(SmartDirtiesSuiteListener::getTestClass));

        SmartDirtiesTestsSupport.setTestClassesLists(ENGINE_TESTNG, testClassesLists);

        return methods;
    }

    private static Class<?> getTestClass(IMethodInstance methodInstance) {
        return methodInstance.getMethod().getTestClass().getRealClass();
    }
}
