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
                "org.codeit.sb06.team03.mopl.content",
                "org.codeit.sb06.team03.mopl.tag",
                "org.codeit.sb06.team03.mopl.liveChatRoom"
        },
        entityManagerFactoryRef = "contentEntityManagerFactory",
        transactionManagerRef = "contentTransactionManager"
)
public class ContentDbConfig {

    @Value("${spring.jpa.content.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Bean
    @ConfigurationProperties("spring.datasource.content")
    public DataSourceProperties contentDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource contentDataSource() {
        return contentDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "contentEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean contentEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.format_sql", "true");

        return builder
                .dataSource(contentDataSource())
                .packages(
                        "org.codeit.sb06.team03.mopl.content",
                        "org.codeit.sb06.team03.mopl.tag",
                        "org.codeit.sb06.team03.mopl.liveChatRoom"
                )
                .persistenceUnit("content")
                .properties(properties)
                .build();
    }

    @Bean(name = "contentTransactionManager")
    public PlatformTransactionManager contentTransactionManager(
            @Qualifier("contentEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
