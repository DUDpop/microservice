package com.pkd.microservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkd.microservice.api.dto.DictionaryDtos;
import com.pkd.microservice.api.exception.GlobalExceptionHandler;
import com.pkd.microservice.domain.service.DictionaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DictionariesController.class)
@Import(GlobalExceptionHandler.class)
class DictionariesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private DictionaryService dictionaryService;

	@Test
	void shouldCreateDictionary() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		DictionaryDtos.DictionaryDto response = new DictionaryDtos.DictionaryDto(
				id,
				"countries",
				"code",
				Map.of("code", "string", "name", "string")
		);

		when(dictionaryService.createDictionary(any(DictionaryDtos.DictionaryCreateRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/dictionaries")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"name", "countries",
								"keyFieldName", "code",
								"schema", Map.of("code", "string", "name", "string")
						))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.name").value("countries"))
				.andExpect(jsonPath("$.schema.code").value("string"));
	}

	@Test
	void shouldCreateRecord() throws Exception {
		UUID dictionaryId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		Map<String, Object> value = Map.of("code", "RU", "name", "Russia");

		when(dictionaryService.createRecord(eq(dictionaryId), any(DictionaryDtos.DictionaryRecordUpsertRequest.class)))
				.thenReturn(value);

		mockMvc.perform(post("/api/dictionaries/{dictionaryId}/records", dictionaryId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"key", "RU",
								"value", value
						))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("RU"))
				.andExpect(jsonPath("$.name").value("Russia"));
	}
}
