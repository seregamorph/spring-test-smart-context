package com.github.seregamorph.testsmartcontext.jdbc;

import java.io.Closeable;
import java.io.IOException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Closeable DataSource which delegates close call to target
 *
 * @author Sergey Chernov
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
        if (!(targetDataSource instanceof AutoCloseable)) {
            throw new IllegalArgumentException("targetDataSource is not AutoCloseable");
        }
        return targetDataSource;
    }

    @Override
    public void close() throws IOException {
        DataSource targetDataSource = getTargetDataSource();
        // condition may be false for lazily initialized DataSource
        if (targetDataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) targetDataSource).close();
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException("Error while closing targetDataSource", e);
            }
        }
    }
}
