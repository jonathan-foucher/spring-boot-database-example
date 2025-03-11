package com.jonathanfoucher.databaseexample.data.repository;

import com.jonathanfoucher.databaseexample.data.dto.FlatMovieDirectorDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MovieDirectorCustomRepository {
    private final EntityManager entityManager;

    public List<FlatMovieDirectorDto> findAllFlatMovieDirectors() {
        String sql = """
                    select m.id movie_id, m.title, m.release_date,
                    d.id director_id, d.first_name, d.last_name
                    from movie m
                    inner join director d on d.id = m.director_id
                    order by d.id, m.id
                """;

        return entityManager.createNativeQuery(sql, FlatMovieDirectorDto.NAME)
                .getResultList();
    }
}
