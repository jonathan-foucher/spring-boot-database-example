package com.jonathanfoucher.databaseexample.data.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "movie")
@Getter
@Setter
public class Movie {
    @Id
    @SequenceGenerator(name = "movie_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movie_id_seq")
    private Long id;
    private Long directorId;
    private String title;
    private LocalDate releaseDate;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
