package com.jonathanfoucher.databaseexample.data.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FlatMovieDirectorDto {
    public static final String NAME = "FlatMovieDirectorDto";

    private Long movieId;
    private String title;
    private LocalDate releaseDate;
    private Long directorId;
    private String firstName;
    private String lastName;

    @Override
    public String toString() {
        return String.format(
                "{ movie_id=%s, title=\"%s\", release_date=%s, director_id=%s, first_name=%s, last_name=%s }",
                movieId, title, releaseDate, directorId, firstName, lastName
        );
    }
}
