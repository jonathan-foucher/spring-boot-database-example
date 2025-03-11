package com.jonathanfoucher.databaseexample.controllers;

import com.jonathanfoucher.databaseexample.data.dto.FlatMovieDirectorDto;
import com.jonathanfoucher.databaseexample.data.dto.MovieDirectorLink;
import com.jonathanfoucher.databaseexample.data.dto.MovieDto;
import com.jonathanfoucher.databaseexample.services.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping("/{id}")
    public MovieDto findById(@PathVariable("id") Long id) {
        return movieService.findById(id);
    }

    @GetMapping
    public Page<MovieDto> findAllFiltered(@PageableDefault(size = 20) Pageable pageable,
                                          @RequestParam(value = "released_after", required = false)
                                          @DateTimeFormat(iso = DATE)
                                          LocalDate releaseAfter,
                                          @RequestParam(value = "updated_since", required = false)
                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS Z")
                                          ZonedDateTime updatedSince) {
        return movieService.findAllFiltered(pageable, releaseAfter, updatedSince);
    }

    @GetMapping("/directors/links")
    public List<MovieDirectorLink> findAllMovieDirectorLinks() {
        return movieService.findAllMovieDirectorLinks();
    }

    @GetMapping("/directors")
    public List<FlatMovieDirectorDto> findAllFlatMovieDirectors() {
        return movieService.findAllFlatMovieDirectors();
    }

    @PostMapping
    public void save(@RequestBody MovieDto movie) {
        movieService.save(movie);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        movieService.deleteById(id);
    }
}
