package com.github.seregamorph.testsmartcontext.mockbean;

import java.util.Set;
import java.util.function.Supplier;
import org.springframework.aop.TargetSource;

class SmartMockTargetSource implements TargetSource {

    private final String name;
    private final Class<?> targetClass;
    private final Supplier<Object> targetSupplier;
    private final Object mock;
    private final Set<Class<?>> testClasses;

    public SmartMockTargetSource(String name, Class<?> targetClass, Supplier<Object> targetSupplier,
                                 Object mock, Set<Class<?>> testClasses) {
        this.name = name;
        this.targetClass = targetClass;
        this.targetSupplier = targetSupplier;
        this.mock = mock;
        this.testClasses = testClasses;
    }

    @Override
    public Class<?> getTargetClass() {
        return targetClass;
    }

    @Override
    public Object getTarget() {
        Class<?> currentTestClass = null;//CurrentTestContext.getCurrentTestClass();
        if (testClasses.contains(currentTestClass)) {
            return mock;
        } else {
            return targetSupplier.get();
        }
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public String toString() {
        return "SmartMockTargetSource{" +
            "name='" + name + '\'' +
            ", targetClass=" + targetClass +
            '}';
    }
}
