package com.pkd.microservice.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Единый обработчик ошибок REST API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	public record ErrorResponse(
			int status,
			String message,
			String path,
			OffsetDateTime timestamp
	) {
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
		ErrorResponse body = new ErrorResponse(
				ex.getStatus().value(),
				ex.getMessage(),
				request.getRequestURI(),
				OffsetDateTime.now()
		);
		return ResponseEntity.status(ex.getStatus()).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.collect(Collectors.joining("; "));

		ErrorResponse body = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				message.isBlank() ? "Ошибка валидации запроса" : message,
				request.getRequestURI(),
				OffsetDateTime.now()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}
}
