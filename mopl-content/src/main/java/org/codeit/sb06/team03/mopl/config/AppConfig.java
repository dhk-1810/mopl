package org.codeit.sb06.team03.mopl.config;

import org.codeit.sb06.team03.mopl.ContentApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan(basePackageClasses = ContentApplication.class)
public class AppConfig {
}
