package com.jonathanfoucher.databaseexample.services;

import com.jonathanfoucher.databaseexample.common.errors.MovieNotFoundException;
import com.jonathanfoucher.databaseexample.data.dto.FlatMovieDirectorDto;
import com.jonathanfoucher.databaseexample.data.dto.MovieDirectorLink;
import com.jonathanfoucher.databaseexample.data.dto.MovieDto;
import com.jonathanfoucher.databaseexample.data.model.Movie;
import com.jonathanfoucher.databaseexample.data.repository.MovieDirectorCustomRepository;
import com.jonathanfoucher.databaseexample.data.repository.MovieRepository;
import com.jonathanfoucher.databaseexample.data.repository.specifications.MovieSpecs;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(MovieService.class)
class MovieServiceTest {
    @Autowired
    private MovieService movieService;
    @MockitoBean
    private MovieRepository movieRepository;
    @MockitoBean
    private MovieDirectorCustomRepository movieDirectorCustomRepository;

    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);
    private static final Long DIRECTOR_ID = 2L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @Test
    void findById() {
        // GIVEN
        Movie movie = initMovie();

        when(movieRepository.findById(ID))
                .thenReturn(Optional.of(movie));

        // WHEN
        MovieDto result = movieService.findById(ID);

        // THEN
        verify(movieRepository, times(1)).findById(ID);

        checkMovieDto(result);
    }

    @Test
    void findByIdWithMovieNotFound() {
        // GIVEN
        when(movieRepository.findById(ID))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> movieService.findById(ID))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie with id 15 not found");

        verify(movieRepository, times(1)).findById(ID);
    }

    @Test
    void findAllFiltered() {
        try (MockedStatic<MovieSpecs> mockedMovieSpecs = mockStatic(MovieSpecs.class)) {
            // GIVEN
            Movie movie = initMovie();
            PageRequest pageRequest = PageRequest.of(0, 20);
            Page<Movie> page = new PageImpl<>(List.of(movie), pageRequest, 1);

            when(movieRepository.findAll(any(Specification.class), eq(pageRequest)))
                    .thenReturn(page);

            // WHEN
            Page<MovieDto> results = movieService.findAllFiltered(pageRequest, null, null);

            // THEN
            mockedMovieSpecs.verify(() -> MovieSpecs.isReleasedAfter(null), times(1));
            mockedMovieSpecs.verify(() -> MovieSpecs.isUpdatedSince(null), times(1));
            verify(movieRepository, times(1)).findAll(any(Specification.class), eq(pageRequest));

            checkMoviePage(results);
        }
    }

    @Test
    void findAllFilteredWithParameters() {
        try (MockedStatic<MovieSpecs> mockedMovieSpecs = mockStatic(MovieSpecs.class)) {
            // GIVEN
            Movie movie = initMovie();

            LocalDate releaseAfter = LocalDate.of(2021, 7, 27);
            ZonedDateTime updatedSince = ZonedDateTime.of(
                    LocalDateTime.of(2021, 7, 27, 3, 4, 32),
                    ZoneOffset.ofHours(2)
            );

            PageRequest pageRequest = PageRequest.of(0, 20);
            Page<Movie> page = new PageImpl<>(List.of(movie), pageRequest, 1);

            when(movieRepository.findAll(any(Specification.class), eq(pageRequest)))
                    .thenReturn(page);

            // WHEN
            Page<MovieDto> results = movieService.findAllFiltered(pageRequest, releaseAfter, updatedSince);

            // THEN
            mockedMovieSpecs.verify(() -> MovieSpecs.isReleasedAfter(releaseAfter), times(1));
            mockedMovieSpecs.verify(() -> MovieSpecs.isUpdatedSince(updatedSince), times(1));
            verify(movieRepository, times(1)).findAll(any(Specification.class), eq(pageRequest));

            checkMoviePage(results);
        }
    }

    @Test
    void findAllMovieDirectorLinks() {
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

        when(movieRepository.findAllMovieDirectorLinks())
                .thenReturn(List.of(link));

        // WHEN
        List<MovieDirectorLink> results = movieService.findAllMovieDirectorLinks();

        // THEN
        verify(movieRepository, times(1)).findAllMovieDirectorLinks();

        assertNotNull(results);
        assertEquals(1, results.size());

        MovieDirectorLink result = results.getFirst();
        assertNotNull(result);
        assertEquals(ID, result.getMovieId());
        assertEquals(DIRECTOR_ID, result.getDirectorId());
    }

    @Test
    void findAllMovieDirectorLinksWithoutResult() {
        // GIVEN
        when(movieRepository.findAllMovieDirectorLinks())
                .thenReturn(emptyList());

        // WHEN
        List<MovieDirectorLink> results = movieService.findAllMovieDirectorLinks();

        // THEN
        verify(movieRepository, times(1)).findAllMovieDirectorLinks();

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void findAllFlatMovieDirectors() {
        // GIVEN
        FlatMovieDirectorDto flatMovieDirector = new FlatMovieDirectorDto();
        flatMovieDirector.setMovieId(ID);
        flatMovieDirector.setTitle(TITLE);
        flatMovieDirector.setReleaseDate(RELEASE_DATE);
        flatMovieDirector.setDirectorId(DIRECTOR_ID);
        flatMovieDirector.setFirstName(FIRST_NAME);
        flatMovieDirector.setLastName(LAST_NAME);

        when(movieDirectorCustomRepository.findAllFlatMovieDirectors())
                .thenReturn(List.of(flatMovieDirector));

        // WHEN
        List<FlatMovieDirectorDto> results = movieService.findAllFlatMovieDirectors();

        // THEN
        verify(movieDirectorCustomRepository, times(1)).findAllFlatMovieDirectors();

        assertNotNull(results);
        assertEquals(1, results.size());

        FlatMovieDirectorDto result = results.getFirst();
        assertNotNull(result);
        assertEquals(ID, result.getMovieId());
        assertEquals(TITLE, result.getTitle());
        assertEquals(RELEASE_DATE, result.getReleaseDate());
        assertEquals(DIRECTOR_ID, result.getDirectorId());
        assertEquals(FIRST_NAME, result.getFirstName());
        assertEquals(LAST_NAME, result.getLastName());
    }

    @Test
    void findAllFlatMovieDirectorsWithoutResult() {
        // GIVEN
        when(movieDirectorCustomRepository.findAllFlatMovieDirectors())
                .thenReturn(emptyList());

        // WHEN
        List<FlatMovieDirectorDto> results = movieService.findAllFlatMovieDirectors();

        // THEN
        verify(movieDirectorCustomRepository, times(1)).findAllFlatMovieDirectors();

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void saveMovieCreated() {
        // GIVEN
        MovieDto movie = initMovieDto();
        movie.setId(null);

        // WHEN
        movieService.save(movie);

        // THEN
        ArgumentCaptor<Movie> capturedMovie = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository, never()).findById(ID);
        verify(movieRepository, times(1)).save(capturedMovie.capture());

        Movie savedMovie = capturedMovie.getValue();
        assertNotNull(savedMovie);
        assertNull(savedMovie.getId());
        assertEquals(DIRECTOR_ID, savedMovie.getDirectorId());
        assertEquals(TITLE, savedMovie.getTitle());
        assertEquals(RELEASE_DATE, savedMovie.getReleaseDate());
    }

    @Test
    void saveMovieCreatedUpdated() {
        // GIVEN
        MovieDto movie = initMovieDto();

        Movie dbMovie = initMovie();
        dbMovie.setReleaseDate(RELEASE_DATE.plusDays(1));

        when(movieRepository.findById(ID))
                .thenReturn(Optional.of(dbMovie));

        // WHEN
        movieService.save(movie);

        // THEN
        ArgumentCaptor<Movie> capturedMovie = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository, times(1)).findById(ID);
        verify(movieRepository, times(1)).save(capturedMovie.capture());

        Movie savedMovie = capturedMovie.getValue();
        assertNotNull(savedMovie);
        assertEquals(ID, savedMovie.getId());
        assertEquals(DIRECTOR_ID, savedMovie.getDirectorId());
        assertEquals(TITLE, savedMovie.getTitle());
        assertEquals(RELEASE_DATE, savedMovie.getReleaseDate());
    }

    @Test
    void saveMovieCreatedUpdatedWithMovieNotFound() {
        // GIVEN
        MovieDto movie = initMovieDto();

        when(movieRepository.findById(ID))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> movieService.save(movie))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie with id 15 not found");

        verify(movieRepository, times(1)).findById(ID);
        verify(movieRepository, never()).save(any());
    }

    @Test
    void deleteById() {
        // GIVEN
        Movie movie = initMovie();

        when(movieRepository.findById(ID))
                .thenReturn(Optional.of(movie));

        // WHEN
        movieService.deleteById(ID);

        // THEN
        ArgumentCaptor<Movie> capturedMovie = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository, times(1)).findById(ID);
        verify(movieRepository, times(1)).delete(capturedMovie.capture());

        Movie deletedMovie = capturedMovie.getValue();
        assertNotNull(deletedMovie);
        assertEquals(ID, deletedMovie.getId());
        assertEquals(DIRECTOR_ID, deletedMovie.getDirectorId());
        assertEquals(TITLE, deletedMovie.getTitle());
        assertEquals(RELEASE_DATE, deletedMovie.getReleaseDate());
    }

    @Test
    void deleteByIdWithMovieNotFound() {
        // GIVEN
        when(movieRepository.findById(ID))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> movieService.deleteById(ID))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie with id 15 not found");

        verify(movieRepository, times(1)).findById(ID);
        verify(movieRepository, never()).delete(any(Movie.class));
    }

    private Movie initMovie() {
        Movie movie = new Movie();
        movie.setId(ID);
        movie.setDirectorId(DIRECTOR_ID);
        movie.setTitle(TITLE);
        movie.setReleaseDate(RELEASE_DATE);
        movie.setUpdatedAt(ZonedDateTime.now().minusDays(2));
        return movie;
    }

    private MovieDto initMovieDto() {
        MovieDto movie = new MovieDto();
        movie.setId(ID);
        movie.setDirectorId(DIRECTOR_ID);
        movie.setTitle(TITLE);
        movie.setReleaseDate(RELEASE_DATE);
        return movie;
    }

    private void checkMovieDto(MovieDto movie) {
        assertNotNull(movie);
        assertEquals(ID, movie.getId());
        assertEquals(TITLE, movie.getTitle());
        assertEquals(RELEASE_DATE, movie.getReleaseDate());
    }

    private void checkMoviePage(Page<MovieDto> page) {
        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertNotNull(page.getContent());
        assertEquals(1, page.getContent().size());

        MovieDto movie = page.getContent().getFirst();
        assertNotNull(movie);
        assertEquals(ID, movie.getId());
        assertEquals(DIRECTOR_ID, movie.getDirectorId());
        assertEquals(TITLE, movie.getTitle());
        assertEquals(RELEASE_DATE, movie.getReleaseDate());
    }
}
