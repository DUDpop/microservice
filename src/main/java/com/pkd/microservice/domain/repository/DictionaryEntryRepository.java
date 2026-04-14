package com.pkd.microservice.domain.repository;

import com.pkd.microservice.domain.entity.DictionaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий записей справочника.
 *
 * Ключ записи хранится в колонке {@code key_value}, поэтому поиск по ключу выполняется быстро.
 */
public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, UUID> {

	Optional<DictionaryEntry> findByDictionary_IdAndKeyValue(UUID dictionaryId, String keyValue);

	boolean existsByDictionary_IdAndKeyValue(UUID dictionaryId, String keyValue);

	void deleteByDictionary_IdAndKeyValue(UUID dictionaryId, String keyValue);
}
