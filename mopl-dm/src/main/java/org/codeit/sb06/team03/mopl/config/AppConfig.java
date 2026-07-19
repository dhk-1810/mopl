package org.codeit.sb06.team03.mopl.config;

import org.codeit.sb06.team03.mopl.DmApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan(basePackageClasses = DmApplication.class)
public class AppConfig {
}
