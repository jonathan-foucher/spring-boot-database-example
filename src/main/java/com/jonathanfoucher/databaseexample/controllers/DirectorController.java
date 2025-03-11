package com.jonathanfoucher.databaseexample.controllers;

import com.jonathanfoucher.databaseexample.data.dto.DirectorDto;
import com.jonathanfoucher.databaseexample.services.DirectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping("/{id}")
    public DirectorDto findById(@PathVariable("id") Long id) {
        return directorService.findById(id);
    }

    @GetMapping("/ordered")
    public List<DirectorDto> findALlByOrderByLastNameAscFirstNameAsc() {
        return directorService.findALlByOrderByLastNameAscFirstNameAsc();
    }

    @GetMapping
    public List<DirectorDto> findByLastName(@RequestParam("last_name") String lastName) {
        return directorService.findByLastName(lastName);
    }

    @PostMapping
    public void save(@RequestBody DirectorDto director) {
        directorService.save(director);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        directorService.deleteById(id);
    }
}
