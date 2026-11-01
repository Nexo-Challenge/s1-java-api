package br.com.nexo.api.exception;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Erros MVC e de integridade; não converte exceções genéricas em conflito ou sucesso. */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    public record ErroCampo(String campo, String mensagem) {}

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridade(DataIntegrityViolationException ex) {
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
            "Operação não permitida por uma restrição de integridade dos dados.");
        body.setTitle("Conflito de integridade");
        return body;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var erros = ex.getBindingResult().getFieldErrors().stream()
            .map(erro -> new ErroCampo(erro.getField(), erro.getDefaultMessage())).toList();
        return handleExceptionInternal(ex, validacao(status, erros), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var erros = ex.getParameterValidationResults().stream().flatMap(resultado ->
            resultado.getResolvableErrors().stream().map(erro -> new ErroCampo(
                erro instanceof FieldError campo ? campo.getField() : resultado.getMethodParameter().getParameterName(),
                erro.getDefaultMessage()))).toList();
        return handleExceptionInternal(ex, validacao(status, erros), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, problema(status, "Parâmetro com tipo inválido."), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, problema(status,
            "Corpo JSON ausente, inválido ou com tipos incompatíveis."), headers, status, request);
    }

    private ProblemDetail validacao(HttpStatusCode status, List<ErroCampo> erros) {
        var body = problema(status, "Verifique os campos informados.");
        body.setProperty("errors", erros.stream()
            .sorted(Comparator.comparing(ErroCampo::campo).thenComparing(ErroCampo::mensagem)).distinct().toList());
        return body;
    }

    private ProblemDetail problema(HttpStatusCode status, String mensagem) {
        var body = ProblemDetail.forStatusAndDetail(status, mensagem);
        body.setTitle(status.is4xxClientError() ? "Requisição inválida" : "Falha interna");
        return body;
    }
}
