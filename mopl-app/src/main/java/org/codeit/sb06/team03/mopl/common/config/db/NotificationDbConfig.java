package org.codeit.sb06.team03.mopl.common.config.db;

import io.github.openfeign.querydsl.jpa.spring.repository.config.EnableQuerydslRepositories;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableQuerydslRepositories(
        basePackages = {
                "org.codeit.sb06.team03.mopl"
        },
        entityManagerFactoryRef = "notificationEntityManagerFactory",
        transactionManagerRef = "notificationTransactionManager"
)
public class NotificationDbConfig {

    @Value("${spring.jpa.notification.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Bean
    @ConfigurationProperties("spring.datasource.notification")
    public DataSourceProperties notificationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource notificationDataSource() {
        return notificationDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "notificationEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean notificationEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.format_sql", "true");

        return builder
                .dataSource(notificationDataSource())
                .packages(
                        "org.codeit.sb06.team03.mopl"
                )
                .persistenceUnit("notification")
                .properties(properties)
                .build();
    }

    @Bean(name = "notificationTransactionManager")
    public PlatformTransactionManager notificationTransactionManager(
            @Qualifier("notificationEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
