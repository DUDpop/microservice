package com.pkd.microservice.domain.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Метаданные справочника:
 * - имя (для клиентов/админки)
 * - название поля, которое используется как ключ для поиска записи
 * - JSON-схема структуры записи (поля/типы могут со временем меняться)
 */
@Entity
@Table(name = "dictionaries")
@Getter
@Setter
public class DictionaryDefinition {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "name", nullable = false, unique = true, length = 200)
	private String name;

	@Column(name = "key_field_name", nullable = false, length = 200)
	private String keyFieldName;

	/**
	 * Храним схему в JSONB. Для сервиса сейчас схема используется в основном как справочная информация,
	 * а ключевое поле - как обязательный элемент для CRUD записей.
	 */
	@Type(JsonType.class)
	@Column(name = "schema_json", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> schema;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	void onCreate() {
		createdAt = OffsetDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = OffsetDateTime.now();
	}
}
