package com.jonathanfoucher.databaseexample.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jonathanfoucher.databaseexample.common.errors.MovieNotFoundException;
import com.jonathanfoucher.databaseexample.controllers.advisers.CustomResponseEntityExceptionHandler;
import com.jonathanfoucher.databaseexample.data.dto.FlatMovieDirectorDto;
import com.jonathanfoucher.databaseexample.data.dto.MovieDirectorLink;
import com.jonathanfoucher.databaseexample.data.dto.MovieDto;
import com.jonathanfoucher.databaseexample.services.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

@WebMvcTest(MovieController.class)
@SpringJUnitConfig({MovieController.class, CustomResponseEntityExceptionHandler.class})
class MovieControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private MovieController movieController;
    @Autowired
    private CustomResponseEntityExceptionHandler customResponseEntityExceptionHandler;
    @MockitoBean
    private MovieService movieService;

    private static final String MOVIE_BY_ID_PATH = "/movies/{id}";
    private static final String MOVIES_PATH = "/movies";
    private static final String MOVIE_DIRECTOR_LINKS_PATH = "/movies/directors/links";
    private static final String FLAT_MOVIE_DIRECTOR_PATH = "/movies/directors";

    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);
    private static final Long DIRECTOR_ID = 2L;
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
        mockMvc = MockMvcBuilders.standaloneSetup(movieController)
                .setControllerAdvice(customResponseEntityExceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void findById() throws Exception {
        // GIVEN
        MovieDto movie = initMovie();

        when(movieService.findById(ID))
                .thenReturn(movie);

        // WHEN / THEN
        mockMvc.perform(get(MOVIE_BY_ID_PATH, ID))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(movie)));

        verify(movieService, times(1)).findById(ID);
    }

    @Test
    void findByIdWithMovieNotFound() throws Exception {
        // GIVEN
        when(movieService.findById(ID))
                .thenThrow(new MovieNotFoundException(ID));

        // WHEN / THEN
        mockMvc.perform(get(MOVIE_BY_ID_PATH, ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", equalTo(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.title", equalTo(NOT_FOUND.getReasonPhrase())))
                .andExpect(jsonPath("$.status", equalTo(NOT_FOUND.value())))
                .andExpect(jsonPath("$.detail", equalTo("Movie with id 15 not found")))
                .andExpect(jsonPath("$.instance", equalTo("uri=/movies/15")))
                .andExpect(jsonPath("$.properties").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp", matchesPattern(TIMESTAMP_REGEX_PATTERN)));

        verify(movieService, times(1)).findById(ID);
    }

    @Test
    void findByIdWithInternalServerError() throws Exception {
        // GIVEN
        when(movieService.findById(ID))
                .thenThrow(new RuntimeException("some error"));

        // WHEN / THEN
        mockMvc.perform(get(MOVIE_BY_ID_PATH, ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type", equalTo(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.title", equalTo(INTERNAL_SERVER_ERROR.getReasonPhrase())))
                .andExpect(jsonPath("$.status", equalTo(INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.detail", equalTo("some error")))
                .andExpect(jsonPath("$.instance", equalTo("uri=/movies/15")))
                .andExpect(jsonPath("$.properties").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp", matchesPattern(TIMESTAMP_REGEX_PATTERN)));

        verify(movieService, times(1)).findById(ID);
    }

    @Test
    void findAllFiltered() throws Exception {
        // GIVEN
        MovieDto movie = initMovie();
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<MovieDto> page = new PageImpl<>(List.of(movie), pageRequest, 1);

        when(movieService.findAllFiltered(pageRequest, null, null))
                .thenReturn(page);

        // WHEN / THEN
        mockMvc.perform(get(MOVIES_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(page)));

        verify(movieService, times(1)).findAllFiltered(pageRequest, null, null);
    }

    @Test
    void findAllFilteredWithEmptyResult() throws Exception {
        // GIVEN
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<MovieDto> page = Page.empty(pageRequest);

        when(movieService.findAllFiltered(pageRequest, null, null))
                .thenReturn(page);

        // WHEN / THEN
        mockMvc.perform(get(MOVIES_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(page)));

        verify(movieService, times(1)).findAllFiltered(pageRequest, null, null);
    }

    @Test
    void findAllFilteredWithParameters() throws Exception {
        // GIVEN
        MovieDto movie = initMovie();

        LocalDate releaseAfter = LocalDate.of(2021, 7, 27);
        ZonedDateTime updatedSince = ZonedDateTime.of(
                LocalDateTime.of(2021, 7, 27, 3, 4, 32),
                ZoneOffset.ofHours(2)
        );

        PageRequest pageRequest = PageRequest.of(3, 40);
        Page<MovieDto> page = new PageImpl<>(List.of(movie), pageRequest, 121);

        when(movieService.findAllFiltered(pageRequest, releaseAfter, updatedSince))
                .thenReturn(page);

        // WHEN / THEN
        mockMvc.perform(get(MOVIES_PATH).queryParam("page", "3")
                        .queryParam("size", "40")
                        .queryParam("released_after", releaseAfter.toString())
                        .queryParam("updated_since", updatedSince.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z"))))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(page)));

        verify(movieService, times(1)).findAllFiltered(pageRequest, releaseAfter, updatedSince);
    }

    @Test
    void findAllFilteredWithPageSizeOverMaximumLimit() throws Exception {
        // GIVEN
        MovieDto movie = initMovie();
        PageRequest pageRequest = PageRequest.of(0, 2000);
        Page<MovieDto> page = new PageImpl<>(List.of(movie), pageRequest, 1);

        when(movieService.findAllFiltered(pageRequest, null, null))
                .thenReturn(page);

        // WHEN / THEN
        mockMvc.perform(get(MOVIES_PATH).queryParam("page", "0")
                        .queryParam("size", "4000"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(page)));

        verify(movieService, times(1)).findAllFiltered(pageRequest, null, null);
    }

    @Test
    void findAllMovieDirectorLinks() throws Exception {
        // GIVEN
        MovieDirectorLink link = new MovieDirectorLink() {
            @Override
            public Long getMovieId() {
                return ID;
            }

            @Override
            public Long getDirectorId() {
                return DIRECTOR_ID;
            }
        };

        when(movieService.findAllMovieDirectorLinks())
                .thenReturn(List.of(link));

        // WHEN / THEN
        mockMvc.perform(get(MOVIE_DIRECTOR_LINKS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(link))));

        verify(movieService, times(1)).findAllMovieDirectorLinks();
    }

    @Test
    void findAllMovieDirectorLinksWithoutResult() throws Exception {
        // GIVEN
        when(movieService.findAllMovieDirectorLinks())
                .thenReturn(emptyList());

        // WHEN / THEN
        mockMvc.perform(get(MOVIE_DIRECTOR_LINKS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(emptyList())));

        verify(movieService, times(1)).findAllMovieDirectorLinks();
    }

    @Test
    void findAllFlatMovieDirectors() throws Exception {
        // GIVEN
        FlatMovieDirectorDto flatMovieDirector = new FlatMovieDirectorDto();
        flatMovieDirector.setMovieId(ID);
        flatMovieDirector.setTitle(TITLE);
        flatMovieDirector.setReleaseDate(RELEASE_DATE);
        flatMovieDirector.setDirectorId(DIRECTOR_ID);
        flatMovieDirector.setFirstName(FIRST_NAME);
        flatMovieDirector.setLastName(LAST_NAME);

        when(movieService.findAllFlatMovieDirectors())
                .thenReturn(List.of(flatMovieDirector));

        // WHEN / THEN
        mockMvc.perform(get(FLAT_MOVIE_DIRECTOR_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(flatMovieDirector))));

        verify(movieService, times(1)).findAllFlatMovieDirectors();
    }

    @Test
    void findAllFlatMovieDirectorsWithoutResult() throws Exception {
        // GIVEN
        when(movieService.findAllFlatMovieDirectors())
                .thenReturn(emptyList());

        // WHEN / THEN
        mockMvc.perform(get(FLAT_MOVIE_DIRECTOR_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(emptyList())));

        verify(movieService, times(1)).findAllFlatMovieDirectors();
    }

    @Test
    void save() throws Exception {
        // GIVEN
        MovieDto movie = initMovie();

        // WHEN / THEN
        mockMvc.perform(post(MOVIES_PATH).contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString()));

        ArgumentCaptor<MovieDto> capturedMovie = ArgumentCaptor.forClass(MovieDto.class);
        verify(movieService, times(1)).save(capturedMovie.capture());

        MovieDto savedMovie = capturedMovie.getAllValues().getFirst();
        checkMovie(savedMovie);
    }

    @Test
    void deleteById() throws Exception {
        // WHEN / THEN
        mockMvc.perform(delete(MOVIE_BY_ID_PATH, ID))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString()));

        verify(movieService, times(1)).deleteById(ID);
    }

    @Test
    void deleteByIdWithMovieNotFound() throws Exception {
        // GIVEN
        doThrow(new MovieNotFoundException(ID))
                .when(movieService).deleteById(ID);

        // WHEN / THEN
        mockMvc.perform(delete(MOVIE_BY_ID_PATH, ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", equalTo(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.title", equalTo(NOT_FOUND.getReasonPhrase())))
                .andExpect(jsonPath("$.status", equalTo(NOT_FOUND.value())))
                .andExpect(jsonPath("$.detail", equalTo("Movie with id 15 not found")))
                .andExpect(jsonPath("$.instance", equalTo("uri=/movies/15")))
                .andExpect(jsonPath("$.properties").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp", matchesPattern(TIMESTAMP_REGEX_PATTERN)));

        verify(movieService, times(1)).deleteById(ID);
    }

    private MovieDto initMovie() {
        MovieDto movie = new MovieDto();
        movie.setId(ID);
        movie.setDirectorId(DIRECTOR_ID);
        movie.setTitle(TITLE);
        movie.setReleaseDate(RELEASE_DATE);
        return movie;
    }

    private void checkMovie(MovieDto movie) {
        assertNotNull(movie);
        assertEquals(ID, movie.getId());
        assertEquals(DIRECTOR_ID, movie.getDirectorId());
        assertEquals(TITLE, movie.getTitle());
        assertEquals(RELEASE_DATE, movie.getReleaseDate());
    }
}
