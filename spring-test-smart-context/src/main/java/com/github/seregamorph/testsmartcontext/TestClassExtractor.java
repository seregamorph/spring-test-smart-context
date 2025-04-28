package com.github.seregamorph.testsmartcontext;

import java.util.function.Function;

public abstract class TestClassExtractor<T> {

    private final ItemType itemType;

    protected TestClassExtractor(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public static <T> TestClassExtractor<T> ofClass(Function<T, Class<?>> testClassExtractor) {
        return new TestClassExtractor<T>(ItemType.TEST_CLASS) {
            @Override
            public Class<?> getTestClass(T test) {
                return testClassExtractor.apply(test);
            }
        };
    }

    public static <T> TestClassExtractor<T> ofMethod(Function<T, Class<?>> testClassExtractor) {
        return new TestClassExtractor<T>(ItemType.TEST_METHOD) {
            @Override
            public Class<?> getTestClass(T test) {
                return testClassExtractor.apply(test);
            }
        };
    }

    public abstract Class<?> getTestClass(T test);

    public enum ItemType {
        TEST_CLASS,
        TEST_METHOD
    }
}
