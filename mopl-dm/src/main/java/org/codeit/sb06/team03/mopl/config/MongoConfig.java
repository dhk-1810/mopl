package org.codeit.sb06.team03.mopl.config;

import org.codeit.sb06.team03.mopl.repository.DMMessageRepository;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "org.codeit.sb06.team03.mopl.repository",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = DMMessageRepository.class
        )
)
public class MongoConfig {
}
