package org.codeit.sb06.team03.mopl.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MoplBatchConfiguration extends DefaultBatchConfiguration {

    private final DataSource batchDataSource;
    private final PlatformTransactionManager batchTransactionManager;

    public MoplBatchConfiguration(
            @Qualifier("batchDataSource") DataSource batchDataSource,
            @Qualifier("batchTransactionManager") PlatformTransactionManager batchTransactionManager) {
        this.batchDataSource = batchDataSource;
        this.batchTransactionManager = batchTransactionManager;
    }

    @Override
    protected DataSource getDataSource() {
        return batchDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return batchTransactionManager;
    }
}
