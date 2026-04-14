package com.pkd.microservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * DTO (контракты) для работы с динамическими справочниками.
 *
 * Схема справочника и сами записи хранятся как JSON-объекты, так как структура может меняться со временем.
 */
public final class DictionaryDtos {
	private DictionaryDtos() {
	}

	public record DictionaryDto(
			UUID id,
			@NotBlank String name,
			@NotBlank String keyFieldName,
			@NotNull Map<String, Object> schema
	) {
	}

	public record DictionaryCreateRequest(
			@NotBlank String name,
			@NotBlank String keyFieldName,
			@NotNull Map<String, Object> schema
	) {
	}

	public record DictionaryUpdateRequest(
			@NotBlank String keyFieldName,
			@NotNull Map<String, Object> schema
	) {
	}

	/**
	 * DTO запроса на создание или обновление записи.
	 * Значение записи передаётся целиком как JSON-объект.
	 */
	public record DictionaryRecordUpsertRequest(
			@NotBlank String key,
			@NotNull Map<String, Object> value
	) {
	}
}
