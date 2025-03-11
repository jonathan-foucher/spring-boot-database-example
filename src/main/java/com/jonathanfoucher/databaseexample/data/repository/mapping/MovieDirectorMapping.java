package com.jonathanfoucher.databaseexample.data.repository.mapping;

import com.jonathanfoucher.databaseexample.data.dto.FlatMovieDirectorDto;
import jakarta.persistence.*;

import java.time.LocalDate;

@SqlResultSetMapping(
        name = FlatMovieDirectorDto.NAME,
        columns = {
                @ColumnResult(name = "movie_id", type = Long.class),
                @ColumnResult(name = "title", type = String.class),
                @ColumnResult(name = "release_date", type = LocalDate.class),
                @ColumnResult(name = "director_id", type = Long.class),
                @ColumnResult(name = "first_name", type = String.class),
                @ColumnResult(name = "last_name", type = String.class)
        }
)
@Entity
@Table(name = "movie")
public class MovieDirectorMapping {
    @Id
    private Long id;
}
