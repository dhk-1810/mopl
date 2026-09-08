package org.codeit.sb06.team03.mopl.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        DataSourceType dataSourceType = isReadOnly ? DataSourceType.SLAVE : DataSourceType.MASTER;
        log.debug("Current transaction readOnly: {}, routing to DataSource: {}", isReadOnly, dataSourceType);
        return dataSourceType;
    }
}
