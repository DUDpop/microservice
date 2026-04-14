package com.pkd.microservice.api.controller;

import com.pkd.microservice.api.dto.DictionaryDtos;
import com.pkd.microservice.domain.service.DictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API для справочников и их записей.
 *
 * Справочники имеют динамическую схему, а записи передаются как JSON-объекты.
 * По ключевому полю запись ищется, обновляется и удаляется.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Dictionaries", description = "API для работы со справочниками и их записями")
public class DictionariesController {

	private final DictionaryService dictionaryService;

	@Operation(summary = "Получить список всех справочников")
	@GetMapping("/dictionaries")
	public List<DictionaryDtos.DictionaryDto> listDictionaries() {
		return dictionaryService.listDictionaries();
	}

	@Operation(summary = "Создать справочник")
	@PostMapping("/dictionaries")
	public ResponseEntity<DictionaryDtos.DictionaryDto> createDictionary(
			@Valid @RequestBody DictionaryDtos.DictionaryCreateRequest request
	) {
		DictionaryDtos.DictionaryDto created = dictionaryService.createDictionary(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@Operation(summary = "Обновить справочник")
	@PutMapping("/dictionaries/{dictionaryId}")
	public DictionaryDtos.DictionaryDto updateDictionary(
			@PathVariable UUID dictionaryId,
			@Valid @RequestBody DictionaryDtos.DictionaryUpdateRequest request
	) {
		return dictionaryService.updateDictionary(dictionaryId, request);
	}

	@Operation(summary = "Удалить справочник и все записи")
	@DeleteMapping("/dictionaries/{dictionaryId}")
	public ResponseEntity<Void> deleteDictionary(@PathVariable UUID dictionaryId) {
		dictionaryService.deleteDictionary(dictionaryId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Добавить запись в справочник")
	@PostMapping("/dictionaries/{dictionaryId}/records")
	public ResponseEntity<Map<String, Object>> createRecord(
			@PathVariable UUID dictionaryId,
			@Valid @RequestBody DictionaryDtos.DictionaryRecordUpsertRequest request
	) {
		Map<String, Object> created = dictionaryService.createRecord(dictionaryId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@Operation(summary = "Получить запись по ключу")
	@GetMapping("/dictionaries/{dictionaryId}/records/{keyValue}")
	public Map<String, Object> getRecord(
			@PathVariable UUID dictionaryId,
			@PathVariable String keyValue
	) {
		return dictionaryService.getRecord(dictionaryId, keyValue);
	}

	@Operation(summary = "Обновить запись по ключу")
	@PutMapping("/dictionaries/{dictionaryId}/records/{keyValue}")
	public Map<String, Object> updateRecord(
			@PathVariable UUID dictionaryId,
			@PathVariable String keyValue,
			@Valid @RequestBody DictionaryDtos.DictionaryRecordUpsertRequest request
	) {
		return dictionaryService.updateRecord(dictionaryId, keyValue, request);
	}

	@Operation(summary = "Удалить запись по ключу")
	@DeleteMapping("/dictionaries/{dictionaryId}/records/{keyValue}")
	public ResponseEntity<Void> deleteRecord(
			@PathVariable UUID dictionaryId,
			@PathVariable String keyValue
	) {
		dictionaryService.deleteRecord(dictionaryId, keyValue);
		return ResponseEntity.noContent().build();
	}
}
