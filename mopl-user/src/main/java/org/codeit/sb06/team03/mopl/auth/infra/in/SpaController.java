package org.codeit.sb06.team03.mopl.auth.infra.in;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/oauth-redirect", "/sign-in"})
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
