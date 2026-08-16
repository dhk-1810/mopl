package org.codeit.sb06.team03.mopl.config;

import io.github.openfeign.querydsl.jpa.spring.repository.config.EnableQuerydslRepositories;
import org.flywaydb.core.Flyway;
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
        entityManagerFactoryRef = "imageEntityManagerFactory",
        transactionManagerRef = "imageTransactionManager"
)
public class ImageDbConfig {

    @Value("${spring.jpa.image.hibernate.ddl-auto:none}")
    private String ddlAuto;

    @Bean
    @ConfigurationProperties("spring.datasource.image")
    public DataSourceProperties imageDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource imageDataSource() {
        DataSource dataSource = imageDataSourceProperties().initializeDataSourceBuilder().build();
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();
        return dataSource;
    }

    @Bean(name = "imageEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean imageEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.format_sql", "true");

        return builder
                .dataSource(imageDataSource())
                .packages(
                        "org.codeit.sb06.team03.mopl"
                )
                .persistenceUnit("image")
                .properties(properties)
                .build();
    }

    @Bean(name = "imageTransactionManager")
    public PlatformTransactionManager imageTransactionManager(
            @Qualifier("imageEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
