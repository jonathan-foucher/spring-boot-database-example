package com.jonathanfoucher.databaseexample.data.repository;

import com.jonathanfoucher.databaseexample.data.dto.MovieDirectorLink;
import com.jonathanfoucher.databaseexample.data.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
    @Query("select m.id as movieId, d.id as directorId " +
            "from Movie m " +
            "inner join Director d on d.id = m.directorId")
    List<MovieDirectorLink> findAllMovieDirectorLinks();
}
