package com.coworkhub.presentation;

import com.coworkhub.business.exception.CredencialesInvalidasException;
import com.coworkhub.business.exception.RecursoNoEncontradoException;
import com.coworkhub.business.exception.ReglaNegocioException;
import com.coworkhub.business.exception.SolicitudInvalidaException;
import com.coworkhub.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.time.Instant;
import java.util.stream.Collectors;

// Un solo lugar donde todas las excepciones se convierten en respuestas JSON con el mismo formato
@RestControllerAdvice
public class ManejadorExcepciones {

    private final Clock clock;

    public ManejadorExcepciones(Clock clock) {
        this.clock = clock;
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorResponse(mensaje, codigo, Instant.now(clock).toString()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException e) {
        return responder(HttpStatus.CONFLICT, e.getCodigo(), e.getMessage());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorResponse> solicitudInvalida(SolicitudInvalidaException e) {
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION", e.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credenciales(CredencialesInvalidasException e) {
        return responder(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", e.getMessage());
    }

    // Falla @Valid en el cuerpo: se juntan los mensajes de todos los campos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION", mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonMalo(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION", "El cuerpo de la peticion no es un JSON valido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoMalo(MethodArgumentTypeMismatchException e) {
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION", "El valor del parametro '" + e.getName() + "' no es valido");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> faltaParametro(MissingServletRequestParameterException e) {
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION", "Falta el parametro '" + e.getParameterName() + "'");
    }

    // @PreAuthorize lanza esta excepcion cuando el usuario no tiene permiso
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException e) {
        return responder(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "No tienes permiso para esta accion");
    }

    // Por si la base de datos rechaza algo (por ejemplo un unique) que no se detecto antes
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException e) {
        return responder(HttpStatus.CONFLICT, "CONFLICTO", "La operacion viola una restriccion de la base de datos");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaNoExiste(NoResourceFoundException e) {
        return responder(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "La ruta no existe");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoPermitido(HttpRequestMethodNotSupportedException e) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO", "Metodo HTTP no permitido en esta ruta");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> otro(Exception e) {
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrio un error inesperado");
    }
}
