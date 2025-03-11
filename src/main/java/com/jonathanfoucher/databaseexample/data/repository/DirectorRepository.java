package com.jonathanfoucher.databaseexample.data.repository;

import com.jonathanfoucher.databaseexample.data.model.Director;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface DirectorRepository extends JpaRepository<Director, Long> {
    Stream<Director> findAllByOrderByLastNameAscFirstNameAsc();

    Stream<Director> findByLastName(String lastName);
}
