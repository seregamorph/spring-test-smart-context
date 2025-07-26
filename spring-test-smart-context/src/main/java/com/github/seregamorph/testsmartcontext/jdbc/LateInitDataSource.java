package com.github.seregamorph.testsmartcontext.jdbc;

import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.lang.Nullable;

/**
 * DataSource decorator which resolves delegate DataSource on first demand.
 * <p>
 * This can be an optimization for DataSources created beyond TestContainers-managed Docker containers: the container is
 * only started if needed.
 * <p>
 * Usage example:
 * <pre>{@code
 *     @Bean
 *     public DataSource dataSource(PostgreSQLContainer<?> container) {
 *         // lazy late initialization - the JDBC url is not known yet, because container is not running
 *         return new LateInitDataSource(() -> {
 *             LOGGER.info("Late initialization data source docker container {}", container);
 *             // start only on demand
 *             container.start();
 *             return createHikariDataSourceForContainer(container);
 *         });
 *     }
 * }</pre>
 * This DataSource delegates close call to the target.
 *
 * @author Sergey Chernov
 */
public class LateInitDataSource extends CloseableDelegatingDataSource {

    @Nullable
    private final String name;
    private final Supplier<DataSource> dataSourceSupplier;

    public LateInitDataSource(Supplier<DataSource> dataSourceSupplier) {
        this(null, dataSourceSupplier);
    }

    public LateInitDataSource(String name, Supplier<DataSource> dataSourceSupplier) {
        this.name = name;
        this.dataSourceSupplier = GuavaSuppliers.memoize(() -> {
            DataSource dataSource = dataSourceSupplier.get();
            setTargetDataSource(dataSource);
            return dataSource;
        });
    }

    @Override
    public void afterPropertiesSet() {
        // no op to skip getTargetDataSource setup
    }

    @Override
    protected DataSource obtainTargetDataSource() {
        return dataSourceSupplier.get();
    }

    @Override
    public String toString() {
        return "LateInitDataSource{" +
            (name == null ? "" : "name='" + name + '\'') +
            ", delegate=" + getTargetDataSource() +
            '}';
    }
}
