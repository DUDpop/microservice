package com.pkd.microservice.domain.repository;

import com.pkd.microservice.domain.entity.DictionaryDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий метаданных справочников.
 */
public interface DictionaryDefinitionRepository extends JpaRepository<DictionaryDefinition, UUID> {

	Optional<DictionaryDefinition> findByName(String name);

	boolean existsByName(String name);
}
