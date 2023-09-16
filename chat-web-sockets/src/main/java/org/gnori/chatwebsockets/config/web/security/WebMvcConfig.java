package org.gnori.chatwebsockets.config.web.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.gnori.chatwebsockets.api.constant.Endpoint.START_PAGE_PATH;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(START_PAGE_PATH).setViewName("index.html");
    }

}
