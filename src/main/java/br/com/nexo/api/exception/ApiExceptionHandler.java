package br.com.nexo.api.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


/** Respostas RFC 9457 para exceções MVC reconhecidas; sem captura genérica. */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
}
