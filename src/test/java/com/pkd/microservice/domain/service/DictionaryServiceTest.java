package com.pkd.microservice.domain.service;

import com.pkd.microservice.api.dto.DictionaryDtos;
import com.pkd.microservice.domain.entity.DictionaryDefinition;
import com.pkd.microservice.domain.entity.DictionaryEntry;
import com.pkd.microservice.domain.repository.DictionaryDefinitionRepository;
import com.pkd.microservice.domain.repository.DictionaryEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

	@Mock
	private DictionaryDefinitionRepository dictionaryDefinitionRepository;

	@Mock
	private DictionaryEntryRepository dictionaryEntryRepository;

	@InjectMocks
	private DictionaryService dictionaryService;

	@Test
	void shouldCreateDictionary() {
		Map<String, Object> schema = Map.of(
				"code", "string",
				"name", "string"
		);
		DictionaryDtos.DictionaryCreateRequest request =
				new DictionaryDtos.DictionaryCreateRequest("countries", "code", schema);

		when(dictionaryDefinitionRepository.existsByName("countries")).thenReturn(false);
		when(dictionaryDefinitionRepository.save(any(DictionaryDefinition.class))).thenAnswer(invocation -> {
			DictionaryDefinition entity = invocation.getArgument(0);
			entity.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
			return entity;
		});

		DictionaryDtos.DictionaryDto result = dictionaryService.createDictionary(request);

		assertThat(result.id()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
		assertThat(result.name()).isEqualTo("countries");
		assertThat(result.keyFieldName()).isEqualTo("code");
		assertThat(result.schema()).isEqualTo(schema);

		ArgumentCaptor<DictionaryDefinition> captor = ArgumentCaptor.forClass(DictionaryDefinition.class);
		verify(dictionaryDefinitionRepository).save(captor.capture());
		assertThat(captor.getValue().getSchema()).isEqualTo(schema);
	}

	@Test
	void shouldCreateRecord() {
		UUID dictionaryId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		Map<String, Object> value = Map.of(
				"code", "RU",
				"name", "Russia"
		);
		DictionaryDtos.DictionaryRecordUpsertRequest request =
				new DictionaryDtos.DictionaryRecordUpsertRequest("RU", value);

		DictionaryDefinition dictionary = new DictionaryDefinition();
		dictionary.setId(dictionaryId);
		dictionary.setName("countries");
		dictionary.setKeyFieldName("code");
		dictionary.setSchema(Map.of("code", "string", "name", "string"));

		when(dictionaryDefinitionRepository.findById(dictionaryId)).thenReturn(java.util.Optional.of(dictionary));
		when(dictionaryEntryRepository.existsByDictionary_IdAndKeyValue(dictionaryId, "RU")).thenReturn(false);
		when(dictionaryEntryRepository.save(any(DictionaryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Map<String, Object> result = dictionaryService.createRecord(dictionaryId, request);

		assertThat(result).isEqualTo(value);

		ArgumentCaptor<DictionaryEntry> captor = ArgumentCaptor.forClass(DictionaryEntry.class);
		verify(dictionaryEntryRepository).save(captor.capture());
		assertThat(captor.getValue().getDictionary()).isEqualTo(dictionary);
		assertThat(captor.getValue().getKeyValue()).isEqualTo("RU");
		assertThat(captor.getValue().getValue()).isEqualTo(value);
	}
}
