package com.github.seregamorph.testsmartcontext;

import java.util.Set;

final class TestClasses {

    private final int order;
    private final Set<Class<?>> classes;

    TestClasses(int order, Set<Class<?>> classes) {
        this.order = order;
        this.classes = classes;
    }

    int order() {
        return order;
    }

    Set<Class<?>> classes() {
        return classes;
    }

    @Override
    public String toString() {
        return "TestClasses[" +
                "order=" + order + ", " +
                "classes=" + classes + ']';
    }
}
