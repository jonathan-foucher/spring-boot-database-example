package com.jonathanfoucher.databaseexample.services;

import com.jonathanfoucher.databaseexample.common.errors.MovieNotFoundException;
import com.jonathanfoucher.databaseexample.data.dto.FlatMovieDirectorDto;
import com.jonathanfoucher.databaseexample.data.dto.MovieDirectorLink;
import com.jonathanfoucher.databaseexample.data.dto.MovieDto;
import com.jonathanfoucher.databaseexample.data.model.Movie;
import com.jonathanfoucher.databaseexample.data.repository.MovieDirectorCustomRepository;
import com.jonathanfoucher.databaseexample.data.repository.MovieRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static com.jonathanfoucher.databaseexample.data.repository.specifications.MovieSpecs.isReleasedAfter;
import static com.jonathanfoucher.databaseexample.data.repository.specifications.MovieSpecs.isUpdatedSince;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final MovieDirectorCustomRepository movieDirectorCustomRepository;

    public MovieDto findById(Long id) {
        Movie movie = findByIdOrThrowNotFound(id);
        return convertEntityToDto(movie);
    }

    public Page<MovieDto> findAllFiltered(Pageable pageable, LocalDate releaseAfter, ZonedDateTime updatedSince) {
        Specification<Movie> specifications = Specification.allOf(
                isReleasedAfter(releaseAfter),
                isUpdatedSince(updatedSince)
        );

        return movieRepository.findAll(specifications, pageable)
                .map(this::convertEntityToDto);
    }

    public List<MovieDirectorLink> findAllMovieDirectorLinks() {
        return movieRepository.findAllMovieDirectorLinks();
    }

    public List<FlatMovieDirectorDto> findAllFlatMovieDirectors() {
        return movieDirectorCustomRepository.findAllFlatMovieDirectors();
    }

    @Transactional
    public void save(MovieDto movie) {
        Movie movieToSave = movie.getId() != null ? findByIdOrThrowNotFound(movie.getId()) : new Movie();
        movieToSave.setId(movie.getId());
        movieToSave.setDirectorId(movie.getDirectorId());
        movieToSave.setTitle(movie.getTitle());
        movieToSave.setReleaseDate(movie.getReleaseDate());
        movieRepository.save(movieToSave);
    }

    @Transactional
    public void deleteById(Long id) {
        Movie movie = findByIdOrThrowNotFound(id);
        movieRepository.delete(movie);
    }

    private Movie findByIdOrThrowNotFound(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    private MovieDto convertEntityToDto(Movie entity) {
        MovieDto dto = new MovieDto();
        dto.setId(entity.getId());
        dto.setDirectorId(entity.getDirectorId());
        dto.setTitle(entity.getTitle());
        dto.setReleaseDate(entity.getReleaseDate());
        return dto;
    }
}
