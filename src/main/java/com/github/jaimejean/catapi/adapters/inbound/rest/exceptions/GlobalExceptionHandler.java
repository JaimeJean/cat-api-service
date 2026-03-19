package com.github.jaimejean.catapi.adapters.inbound.rest.exceptions;

import com.github.jaimejean.catapi.domain.exceptions.BreedNotFoundException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BreedNotFoundException.class)
  public ProblemDetail handleBreedNotFound(BreedNotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setTitle("Breed Not Found");
    problem.setType(URI.create("https://api.catapi.com/errors/breed-not-found"));

    return problem;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    log.warn("Invalid parameter: {}", ex.getMessage());

    String detail =
        String.format(
            "Parameter '%s' must be of type %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    problem.setTitle("Invalid Parameter");
    problem.setType(URI.create("https://api.catapi.com/errors/invalid-parameter"));

    return problem;
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
    log.warn("Endpoint not found: {}", ex.getMessage());

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, "The requested endpoint does not exist");
    problem.setTitle("Endpoint Not Found");
    problem.setType(URI.create("https://api.catapi.com/errors/endpoint-not-found"));

    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later.");
    problem.setTitle("Internal Server Error");
    problem.setType(URI.create("https://api.catapi.com/errors/internal-error"));

    return problem;
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
    log.warn("Missing required parameter: {}", ex.getParameterName());

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setTitle("Missing Parameter");
    problem.setType(URI.create("https://api.catapi.com/errors/missing-parameter"));

    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Validation Error");
    problem.setDetail(
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Invalid request body"));
    return ResponseEntity.badRequest().body(problem);
  }
}
