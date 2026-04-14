package com.pkd.microservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Настройка метаданных OpenAPI для Swagger UI.
 * Springdoc использует это описание для отображения информации о сервисе.
 */
@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Microservice (Dictionaries)",
				version = "0.0.1",
				description = "REST API для работы со справочниками и динамическими JSON-записями"
		)
)
public class OpenApiConfig {
	// Конфигурация задаётся через аннотацию @OpenAPIDefinition.
}
