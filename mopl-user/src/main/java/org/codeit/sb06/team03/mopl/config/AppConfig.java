package org.codeit.sb06.team03.mopl.config;

import org.codeit.sb06.team03.mopl.UserApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan(basePackageClasses = UserApplication.class)
public class AppConfig {
}
