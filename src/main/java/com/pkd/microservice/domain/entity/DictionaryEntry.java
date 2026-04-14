package com.pkd.microservice.domain.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 * Запись внутри конкретного справочника.
 *
 * Структура записи динамическая: все поля хранятся в JSONB.
 * Для ускорения поиска по ключевому полю значение ключа выделяется в отдельную колонку {@code key_value}.
 */
@Entity
@Table(name = "dictionary_entries")
@Getter
@Setter
public class DictionaryEntry {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "dictionary_id", nullable = false)
	private DictionaryDefinition dictionary;

	@Column(name = "key_value", nullable = false)
	private String keyValue;

	@Type(JsonType.class)
	@Column(name = "value_json", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> value;

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
