package com.ford.vehicleapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Padroniza TODAS as respostas de erro da API no formato Problem Details (RFC 9457 / 7807).
 * <p>
 * Também recebe as falhas de autenticação (401) e autorização (403) vindas dos filtros do
 * Spring Security, que são delegadas para cá por {@code RestAuthenticationEntryPoint} e
 * {@code RestAccessDeniedHandler}, garantindo um único formato de erro.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERROR_TYPE_BASE = "https://api.ford-catalog.dev/errors/";
    private static final URI ABOUT_BLANK = URI.create("about:blank");
    private static final Map<Integer, String> FRAMEWORK_TITLES = Map.of(
            400, "Requisição inválida",
            404, "Recurso não encontrado",
            405, "Método não permitido",
            406, "Formato de resposta não suportado",
            415, "Tipo de conteúdo não suportado");

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "not-found", "Recurso não encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "conflict", "Conflito", ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "business-rule", "Regra de negócio violada", ex.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        String detail;
        if (ex instanceof BadCredentialsException) {
            detail = "E-mail ou senha inválidos";
        } else if (ex instanceof InvalidBearerTokenException) {
            detail = "Token JWT inválido ou expirado";
        } else {
            detail = "Autenticação necessária. Envie um token JWT válido no header Authorization: Bearer <token>";
        }
        ResponseEntity<ProblemDetail> response = problem(HttpStatus.UNAUTHORIZED, "unauthorized", "Não autenticado", detail, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .body(response.getBody());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "forbidden", "Acesso negado",
                "Seu perfil não tem permissão para acessar este recurso", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    /** Erros de Bean Validation (@Valid) -> 400 com a lista de campos inválidos. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.<String, Object>of(
                        "field", error.getField(),
                        "message", String.valueOf(error.getDefaultMessage())))
                .toList();

        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos são inválidos");
        body.setTitle("Requisição inválida");
        body.setType(URI.create(ERROR_TYPE_BASE + "validation"));
        body.setProperty("errors", errors);
        return createResponseEntity(body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Corpo da requisição ausente, malformado ou com valores inválidos");
        body.setTitle("Requisição inválida");
        body.setType(URI.create(ERROR_TYPE_BASE + "malformed-request"));
        return createResponseEntity(body, headers, HttpStatus.BAD_REQUEST, request);
    }

    /** Ponto único em que os erros tratados pela classe base (405, 415, 404 de rota...) recebem timestamp e instance. */
    @Override
    protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers, HttpStatusCode statusCode,
                                                          WebRequest request) {
        if (body instanceof ProblemDetail problemDetail) {
            enrich(problemDetail, ((ServletWebRequest) request).getRequest());
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String type, String title, String detail,
                                                  HttpServletRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(title);
        body.setType(URI.create(ERROR_TYPE_BASE + type));
        enrich(body, request);
        return ResponseEntity.status(status).body(body);
    }

    private void enrich(ProblemDetail body, HttpServletRequest request) {
        if (body.getType() == null || ABOUT_BLANK.equals(body.getType())) {
            // Erros do próprio Spring MVC (405, 415, 404 de rota...) ganham type e título no mesmo padrão dos demais.
            HttpStatus status = HttpStatus.resolve(body.getStatus());
            String slug = status != null ? status.name().toLowerCase().replace('_', '-') : "http-" + body.getStatus();
            body.setType(URI.create(ERROR_TYPE_BASE + slug));
            body.setTitle(FRAMEWORK_TITLES.getOrDefault(body.getStatus(), body.getTitle()));
        }
        body.setInstance(URI.create(request.getRequestURI()));
        body.setProperty("timestamp", Instant.now());
    }
}
