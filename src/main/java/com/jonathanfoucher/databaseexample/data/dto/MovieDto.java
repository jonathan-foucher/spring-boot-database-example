package com.jonathanfoucher.databaseexample.data.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MovieDto {
    private Long id;
    private Long directorId;
    private String title;
    private LocalDate releaseDate;

    @Override
    public String toString() {
        return String.format(
                "{ id=%s, director_id=%s, title=\"%s\", release_date=%s }",
                id, directorId, title, releaseDate
        );
    }
}
