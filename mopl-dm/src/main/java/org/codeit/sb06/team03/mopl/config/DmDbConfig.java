package org.codeit.sb06.team03.mopl.config;

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
        entityManagerFactoryRef = "dmEntityManagerFactory",
        transactionManagerRef = "dmTransactionManager"
)
public class DmDbConfig {

    @Value("${spring.jpa.dm.hibernate.ddl-auto:none}")
    private String ddlAuto;

    @Bean
    @ConfigurationProperties("spring.datasource.dm")
    public DataSourceProperties dmDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dmDataSource() {
        return dmDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "dmEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dmEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.format_sql", "true");

        return builder
                .dataSource(dmDataSource())
                .packages(
                        "org.codeit.sb06.team03.mopl"
                )
                .persistenceUnit("dm")
                .properties(properties)
                .build();
    }

    @Bean(name = "dmTransactionManager")
    public PlatformTransactionManager dmTransactionManager(
            @Qualifier("dmEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
