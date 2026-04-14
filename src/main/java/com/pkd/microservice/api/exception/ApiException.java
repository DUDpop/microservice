package com.pkd.microservice.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Единый тип ошибок API.
 * Вынесен отдельно, чтобы сервисы могли бросать исключения с HTTP-кодом.
 */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
