package com.github.seregamorph.testsmartcontext.jdbc;

import java.io.Closeable;
import java.io.IOException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Closeable DataSource which delegates close call to target
 */
public class CloseableDelegatingDataSource extends DelegatingDataSource implements Closeable {

    public CloseableDelegatingDataSource() {
        // for lazy initialization
    }

    public CloseableDelegatingDataSource(DataSource targetDataSource) {
        // eagerly initialized DataSource should be closeable
        super(requireCloseable(targetDataSource));
    }

    private static DataSource requireCloseable(DataSource targetDataSource) {
        if (targetDataSource == null) {
            throw new IllegalArgumentException("targetDataSource is null");
        }
        if (!(targetDataSource instanceof Closeable)) {
            throw new IllegalArgumentException("targetDataSource is not closeable");
        }
        return targetDataSource;
    }

    @Override
    public void close() throws IOException {
        DataSource targetDataSource = getTargetDataSource();
        // condition may be false for lazily initialized DataSource
        if (targetDataSource instanceof Closeable) {
            ((Closeable) targetDataSource).close();
        }
    }
}
