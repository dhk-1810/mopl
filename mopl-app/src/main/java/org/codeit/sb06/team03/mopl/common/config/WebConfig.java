package org.codeit.sb06.team03.mopl.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/oauth-redirect").setViewName("forward:/index.html");
        registry.addViewController("/sign-in").setViewName("forward:/index.html");
    }
}
