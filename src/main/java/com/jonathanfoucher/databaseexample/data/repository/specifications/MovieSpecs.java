package com.jonathanfoucher.databaseexample.data.repository.specifications;

import com.jonathanfoucher.databaseexample.data.model.Movie;
import com.jonathanfoucher.databaseexample.data.model.Movie_;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public abstract class MovieSpecs {
    public static Specification<Movie> isReleasedAfter(LocalDate releaseAfter) {
        return (root, query, builder) -> releaseAfter != null ? builder.greaterThan(root.get(Movie_.releaseDate), releaseAfter) : null;
    }

    public static Specification<Movie> isUpdatedSince(ZonedDateTime updatedSince) {
        return (root, query, builder) -> updatedSince != null ? builder.greaterThan(root.get(Movie_.updatedAt), updatedSince) : null;
    }
}
