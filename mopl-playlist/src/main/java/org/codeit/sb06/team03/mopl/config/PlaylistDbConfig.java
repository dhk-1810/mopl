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
                "org.codeit.sb06.team03.mopl.repository"
        },
        entityManagerFactoryRef = "playlistEntityManagerFactory",
        transactionManagerRef = "playlistTransactionManager"
)
public class PlaylistDbConfig {

    @Value("${spring.jpa.playlist.hibernate.ddl-auto:none}")
    private String ddlAuto;

    @Bean
    @ConfigurationProperties("spring.datasource.playlist")
    public DataSourceProperties playlistDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource playlistDataSource() {
        return playlistDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "playlistEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean playlistEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.format_sql", "true");

        return builder
                .dataSource(playlistDataSource())
                .packages(
                        "org.codeit.sb06.team03.mopl.entity",
                        "org.codeit.sb06.team03.mopl.entity.cqrs",
                        "org.codeit.sb06.team03.mopl.image.domain.entity"
                )
                .persistenceUnit("playlist")
                .properties(properties)
                .build();
    }

    @Bean(name = "playlistTransactionManager")
    public PlatformTransactionManager playlistTransactionManager(
            @Qualifier("playlistEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
