package com.github.seregamorph.testsmartcontext.jdbc;

import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.lang.Nullable;

/**
 * Original code:
 * <a href="https://github.com/google/guava/blob/master/guava/src/com/google/common/base/Suppliers.java">Guava
 * Suppliers.java</a>
 */
final class GuavaSuppliers {

    /**
     * Returns a supplier which caches the instance retrieved during the first call to {@code get()}
     * and returns that value on subsequent calls to {@code get()}. See: <a
     * href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
     *
     * <p>The returned supplier is thread-safe. The delegate's {@code get()} method will be invoked at
     * most once unless the underlying {@code get()} throws an exception.
     *
     * <p>When the underlying delegate throws an exception then this memoizing supplier will keep
     * delegating calls until it returns valid data.
     *
     * <p>If {@code delegate} is an instance created by an earlier call to {@code memoize}, it is
     * returned directly.
     */
    static <T> Supplier<T> memoize(Supplier<T> delegate) {
        if (delegate instanceof NonSerializableMemoizingSupplier) {
            return delegate;
        }
        return new NonSerializableMemoizingSupplier<>(delegate);
    }

    static class NonSerializableMemoizingSupplier<T> implements Supplier<T> {
        volatile Supplier<T> delegate;
        volatile boolean initialized;
        // "value" does not need to be volatile; visibility piggy-backs
        // on volatile read of "initialized".
        @Nullable
        T value;

        NonSerializableMemoizingSupplier(Supplier<T> delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public T get() {
            // A 2-field variant of Double Checked Locking.
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        T t = delegate.get();
                        value = t;
                        initialized = true;
                        // Release the delegate to GC.
                        delegate = null;
                        return t;
                    }
                }
            }
            return value;
        }

        @Override
        public String toString() {
            Supplier<T> delegate = this.delegate;
            return "Suppliers.memoize("
                + (delegate == null ? "<supplier that returned " + value + ">" : delegate)
                + ")";
        }
    }
}
