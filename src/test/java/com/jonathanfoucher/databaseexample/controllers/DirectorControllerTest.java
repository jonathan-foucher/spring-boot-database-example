package com.jonathanfoucher.databaseexample.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jonathanfoucher.databaseexample.common.errors.DirectorNotFoundException;
import com.jonathanfoucher.databaseexample.controllers.advisers.CustomResponseEntityExceptionHandler;
import com.jonathanfoucher.databaseexample.data.dto.DirectorDto;
import com.jonathanfoucher.databaseexample.services.DirectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DirectorController.class)
@SpringJUnitConfig({DirectorController.class, CustomResponseEntityExceptionHandler.class})
class DirectorControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private DirectorController directorController;
    @Autowired
    private CustomResponseEntityExceptionHandler customResponseEntityExceptionHandler;
    @MockitoBean
    private DirectorService directorService;

    private static final String DIRECTOR_BY_ID_PATH = "/directors/{id}";
    private static final String DIRECTORS_ORDERED_PATH = "/directors/ordered";
    private static final String DIRECTORS_PATH = "/directors";

    private static final Long ID = 2L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    private static final Pattern TIMESTAMP_REGEX_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
    private static final String DEFAULT_TYPE = "about:blank";

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .propertyNamingStrategy(SNAKE_CASE)
                .build();
    }

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(directorController)
                .setControllerAdvice(customResponseEntityExceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void findById() throws Exception {
        // GIVEN
        DirectorDto director = initDirector();

        when(directorService.findById(ID))
                .thenReturn(director);

        // WHEN / THEN
        mockMvc.perform(get(DIRECTOR_BY_ID_PATH, ID))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(director)));

        verify(directorService, times(1)).findById(ID);
    }

    @Test
    void findByIdWithDirectorNotFound() throws Exception {
        // GIVEN
        when(directorService.findById(ID))
                .thenThrow(new DirectorNotFoundException(ID));

        // WHEN / THEN
        mockMvc.perform(get(DIRECTOR_BY_ID_PATH, ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", equalTo(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.title", equalTo(NOT_FOUND.getReasonPhrase())))
                .andExpect(jsonPath("$.status", equalTo(NOT_FOUND.value())))
                .andExpect(jsonPath("$.detail", equalTo("Director with id 2 not found")))
                .andExpect(jsonPath("$.instance", equalTo("uri=/directors/2")))
                .andExpect(jsonPath("$.properties").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp", matchesPattern(TIMESTAMP_REGEX_PATTERN)));

        verify(directorService, times(1)).findById(ID);
    }

    @Test
    void findByIdWithInternalServerError() throws Exception {
        // GIVEN
        when(directorService.findById(ID))
                .thenThrow(new RuntimeException("some error"));

        // WHEN / THEN
        mockMvc.perform(get(DIRECTOR_BY_ID_PATH, ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type", equalTo(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.title", equalTo(INTERNAL_SERVER_ERROR.getReasonPhrase())))
                .andExpect(jsonPath("$.status", equalTo(INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.detail", equalTo("some error")))
                .andExpect(jsonPath("$.instance", equalTo("uri=/directors/2")))
                .andExpect(jsonPath("$.properties").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp", matchesPattern(TIMESTAMP_REGEX_PATTERN)));

        verify(directorService, times(1)).findById(ID);
    }

    @Test
    void findAllByOrderByLastNameAscFirstNameAsc() throws Exception {
        // GIVEN
        DirectorDto director = initDirector();

        when(directorService.findAllByOrderByLastNameAscFirstNameAsc())
                .thenReturn(List.of(director));

        // WHEN / THEN
        mockMvc.perform(get(DIRECTORS_ORDERED_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(director))));

        verify(directorService, times(1)).findAllByOrderByLastNameAscFirstNameAsc();
    }

    @Test
    void findAllByOrderByLastNameAscFirstNameAscWithoutResult() throws Exception {
        // GIVEN
        when(directorService.findAllByOrderByLastNameAscFirstNameAsc())
                .thenReturn(emptyList());

        // WHEN / THEN
        mockMvc.perform(get(DIRECTORS_ORDERED_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(emptyList())));

        verify(directorService, times(1)).findAllByOrderByLastNameAscFirstNameAsc();
    }

    @Test
    void findByLastName() throws Exception {
        // GIVEN
        DirectorDto director = initDirector();

        when(directorService.findByLastName(LAST_NAME))
                .thenReturn(List.of(director));

        // WHEN / THEN
        mockMvc.perform(get(DIRECTORS_PATH).queryParam("last_name", LAST_NAME))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(director))));

        verify(directorService, times(1)).findByLastName(LAST_NAME);
    }

    @Test
    void findByLastNameWithoutResult() throws Exception {
        // GIVEN
        when(directorService.findByLastName(LAST_NAME))
                .thenReturn(emptyList());

        // WHEN / THEN
        mockMvc.perform(get(DIRECTORS_PATH).queryParam("last_name", LAST_NAME))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(emptyList())));

        verify(directorService, times(1)).findByLastName(LAST_NAME);
    }

    @Test
    void save() throws Exception {
        // GIVEN
        DirectorDto director = initDirector();

        // WHEN / THEN
        mockMvc.perform(post(DIRECTORS_PATH).contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(director)))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString()));

        ArgumentCaptor<DirectorDto> capturedDirector = ArgumentCaptor.forClass(DirectorDto.class);
        verify(directorService, times(1)).save(capturedDirector.capture());

        DirectorDto savedDirector = capturedDirector.getAllValues().getFirst();
        checkDirector(savedDirector);
    }

    @Test
    void deleteById() throws Exception {
        // WHEN / THEN
        mockMvc.perform(delete(DIRECTOR_BY_ID_PATH, ID))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString()));

        verify(directorService, times(1)).deleteById(ID);
    }

    @Test
    void deleteByIdWithDirectorNotFound() throws Exception {
        // GIVEN
        doThrow(new DirectorNotFoundException(ID))
                .when(directorService).deleteById(ID);

        // WHEN / THEN
        mockMvc.perform(delete(DIRECTOR_BY_ID_PATH, ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", equalTo(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.title", equalTo(NOT_FOUND.getReasonPhrase())))
                .andExpect(jsonPath("$.status", equalTo(NOT_FOUND.value())))
                .andExpect(jsonPath("$.detail", equalTo("Director with id 2 not found")))
                .andExpect(jsonPath("$.instance", equalTo("uri=/directors/2")))
                .andExpect(jsonPath("$.properties").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp", matchesPattern(TIMESTAMP_REGEX_PATTERN)));

        verify(directorService, times(1)).deleteById(ID);
    }

    private DirectorDto initDirector() {
        DirectorDto director = new DirectorDto();
        director.setId(ID);
        director.setFirstName(FIRST_NAME);
        director.setLastName(LAST_NAME);
        return director;
    }

    private void checkDirector(DirectorDto director) {
        assertNotNull(director);
        assertEquals(ID, director.getId());
        assertEquals(FIRST_NAME, director.getFirstName());
        assertEquals(LAST_NAME, director.getLastName());
    }
}
