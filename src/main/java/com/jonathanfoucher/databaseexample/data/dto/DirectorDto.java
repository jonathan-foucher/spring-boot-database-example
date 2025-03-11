package com.jonathanfoucher.databaseexample.data.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectorDto {
    private Long id;
    private String firstName;
    private String lastName;

    @Override
    public String toString() {
        return String.format("{ id=%s, first_name=\"%s\", last_name=%s }", id, firstName, lastName);
    }
}
