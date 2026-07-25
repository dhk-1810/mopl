package org.codeit.sb06.team03.mopl.exception;

import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.controller.AuthController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = AuthController.class)
public class AuthControllerAdvice {

}
