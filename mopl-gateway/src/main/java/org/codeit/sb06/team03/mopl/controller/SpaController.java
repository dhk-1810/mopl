package org.codeit.sb06.team03.mopl.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SpaController {

    @GetMapping(value = {"/oauth-redirect", "/sign-in"}, produces = MediaType.TEXT_HTML_VALUE)
    public Mono<Resource> forwardToSpa() {
        return Mono.just(new ClassPathResource("static/index.html"));
    }
}
