package com.pkd.microservice.domain.service;

import com.pkd.microservice.api.dto.DictionaryDtos;
import com.pkd.microservice.api.exception.ApiException;
import com.pkd.microservice.domain.entity.DictionaryDefinition;
import com.pkd.microservice.domain.entity.DictionaryEntry;
import com.pkd.microservice.domain.repository.DictionaryDefinitionRepository;
import com.pkd.microservice.domain.repository.DictionaryEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Бизнес-логика справочников и их записей.
 *
 * Сервис работает с динамической структурой через JSON:
 * - метаданные справочника хранятся в таблице {@code dictionaries}
 * - записи справочника хранятся в {@code dictionary_entries} как JSONB
 * - ключ записи дублируется в колонке {@code key_value} для быстрого поиска
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DictionaryService {

	private final DictionaryDefinitionRepository dictionaryDefinitionRepository;
	private final DictionaryEntryRepository dictionaryEntryRepository;

	/**
	 * Возвращает список всех доступных справочников.
	 */
	public List<DictionaryDtos.DictionaryDto> listDictionaries() {
		log.info("Получение списка всех справочников");
		return dictionaryDefinitionRepository.findAll().stream()
				.map(this::toDictionaryDto)
				.toList();
	}

	/**
	 * Создаёт новый справочник с указанной схемой и ключевым полем.
	 */
	public DictionaryDtos.DictionaryDto createDictionary(DictionaryDtos.DictionaryCreateRequest request) {
		log.info("Создание справочника: name={}, keyFieldName={}", request.name(), request.keyFieldName());
		if (dictionaryDefinitionRepository.existsByName(request.name())) {
			log.warn("Справочник уже существует: name={}", request.name());
			throw new ApiException(HttpStatus.CONFLICT, "Справочник с таким именем уже существует");
		}

		DictionaryDefinition entity = new DictionaryDefinition();
		entity.setName(request.name());
		entity.setKeyFieldName(request.keyFieldName());
		entity.setSchema(request.schema());

		DictionaryDefinition saved = dictionaryDefinitionRepository.save(entity);
		log.info("Справочник создан: id={}, name={}", saved.getId(), saved.getName());
		return toDictionaryDto(saved);
	}

	/**
	 * Обновляет метаданные справочника: ключевое поле и JSON-схему.
	 * Важно: существующие записи не валидируются относительно новой схемы.
	 */
	public DictionaryDtos.DictionaryDto updateDictionary(UUID dictionaryId, DictionaryDtos.DictionaryUpdateRequest request) {
		log.info("Обновление справочника: dictionaryId={}, keyFieldName={}", dictionaryId, request.keyFieldName());
		DictionaryDefinition existing = dictionaryDefinitionRepository.findById(dictionaryId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Справочник не найден"));

		existing.setKeyFieldName(request.keyFieldName());
		existing.setSchema(request.schema());
		DictionaryDefinition saved = dictionaryDefinitionRepository.save(existing);

		log.info("Справочник обновлён: id={}", saved.getId());
		return toDictionaryDto(saved);
	}

	/**
	 * Удаляет справочник и все его записи по каскаду внешнего ключа.
	 */
	public void deleteDictionary(UUID dictionaryId) {
		log.info("Удаление справочника: dictionaryId={}", dictionaryId);
		if (!dictionaryDefinitionRepository.existsById(dictionaryId)) {
			log.warn("Попытка удалить несуществующий справочник: dictionaryId={}", dictionaryId);
			throw new ApiException(HttpStatus.NOT_FOUND, "Справочник не найден");
		}
		dictionaryDefinitionRepository.deleteById(dictionaryId);
		log.info("Справочник удалён: dictionaryId={}", dictionaryId);
	}

	/**
	 * Возвращает запись справочника по ключевому значению.
	 */
	public Map<String, Object> getRecord(UUID dictionaryId, String keyValue) {
		log.info("Получение записи: dictionaryId={}, key={}", dictionaryId, keyValue);
		DictionaryEntry entry = dictionaryEntryRepository.findByDictionary_IdAndKeyValue(dictionaryId, keyValue)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Запись не найдена"));
		return entry.getValue();
	}

	/**
	 * Создаёт запись в справочник.
	 * Если запись с таким ключом уже существует, возвращается ошибка 409.
	 */
	public Map<String, Object> createRecord(UUID dictionaryId, DictionaryDtos.DictionaryRecordUpsertRequest request) {
		log.info("Добавление записи: dictionaryId={}, key={}", dictionaryId, request.key());
		DictionaryDefinition dictionary = getDictionaryOrThrow(dictionaryId);
		validateKeyMatchesValue(dictionary, request.key(), request.value());

		String keyValue = request.key();
		boolean exists = dictionaryEntryRepository.existsByDictionary_IdAndKeyValue(dictionaryId, keyValue);
		if (exists) {
			log.warn("Запись уже существует: dictionaryId={}, key={}", dictionaryId, keyValue);
			throw new ApiException(HttpStatus.CONFLICT, "Запись с таким ключом уже существует");
		}

		DictionaryEntry entry = new DictionaryEntry();
		entry.setDictionary(dictionary);
		entry.setKeyValue(keyValue);
		entry.setValue(request.value());

		DictionaryEntry saved = dictionaryEntryRepository.save(entry);
		log.info("Запись добавлена: dictionaryId={}, key={}", dictionaryId, saved.getKeyValue());
		return saved.getValue();
	}

	/**
	 * Обновляет запись по ключевому значению.
	 * Если запись не найдена, возвращается ошибка 404.
	 */
	public Map<String, Object> updateRecord(UUID dictionaryId, String keyInPath, DictionaryDtos.DictionaryRecordUpsertRequest request) {
		log.info("Обновление записи: dictionaryId={}, key={}", dictionaryId, keyInPath);
		DictionaryDefinition dictionary = getDictionaryOrThrow(dictionaryId);
		if (!keyInPath.equals(request.key())) {
			log.warn("Ключ в URL не совпадает с payload: dictionaryId={}, pathKey={}, payloadKey={}",
					dictionaryId, keyInPath, request.key());
			throw new ApiException(HttpStatus.BAD_REQUEST, "Ключ в URL и ключ в payload не совпадают");
		}
		validateKeyMatchesValue(dictionary, request.key(), request.value());

		DictionaryEntry existing = dictionaryEntryRepository.findByDictionary_IdAndKeyValue(dictionaryId, keyInPath)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Запись не найдена"));

		existing.setValue(request.value());
		DictionaryEntry saved = dictionaryEntryRepository.save(existing);
		log.info("Запись обновлена: dictionaryId={}, key={}", dictionaryId, saved.getKeyValue());
		return saved.getValue();
	}

	/**
	 * Удаляет запись справочника по ключевому значению.
	 */
	public void deleteRecord(UUID dictionaryId, String keyValue) {
		log.info("Удаление записи: dictionaryId={}, key={}", dictionaryId, keyValue);
		if (!dictionaryEntryRepository.existsByDictionary_IdAndKeyValue(dictionaryId, keyValue)) {
			log.warn("Попытка удалить несуществующую запись: dictionaryId={}, key={}", dictionaryId, keyValue);
			throw new ApiException(HttpStatus.NOT_FOUND, "Запись не найдена");
		}
		dictionaryEntryRepository.deleteByDictionary_IdAndKeyValue(dictionaryId, keyValue);
		log.info("Запись удалена: dictionaryId={}, key={}", dictionaryId, keyValue);
	}

	private DictionaryDefinition getDictionaryOrThrow(UUID dictionaryId) {
		return dictionaryDefinitionRepository.findById(dictionaryId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Справочник не найден"));
	}

	/**
	 * Проверяет, что ключевое поле присутствует в payload и совпадает с ключом запроса.
	 */
	private void validateKeyMatchesValue(DictionaryDefinition dictionary, String key, Map<String, Object> value) {
		Object keyNode = value.get(dictionary.getKeyFieldName());
		if (keyNode == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"В записи отсутствует ключевое поле: " + dictionary.getKeyFieldName());
		}

		if (keyNode instanceof String stringValue) {
			if (stringValue.isBlank()) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Ключевое поле не должно быть пустым");
			}
		} else if (!(keyNode instanceof Number) && !(keyNode instanceof Boolean)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Ключевое поле должно быть строкой/числом/boolean");
		}

		if (!key.equals(String.valueOf(keyNode))) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Ключ в payload не совпадает с keyFieldName");
		}
	}

	private DictionaryDtos.DictionaryDto toDictionaryDto(DictionaryDefinition entity) {
		return new DictionaryDtos.DictionaryDto(
				entity.getId(),
				entity.getName(),
				entity.getKeyFieldName(),
				entity.getSchema()
		);
	}
}
