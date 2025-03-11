package com.jonathanfoucher.databaseexample.data.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "director")
@Getter
@Setter
public class Director {
    @Id
    @SequenceGenerator(name = "director_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "director_id_seq")
    private Long id;
    private String firstName;
    private String lastName;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
