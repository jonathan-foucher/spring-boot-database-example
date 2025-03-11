package com.jonathanfoucher.databaseexample.services;

import com.jonathanfoucher.databaseexample.common.errors.DirectorNotFoundException;
import com.jonathanfoucher.databaseexample.data.dto.DirectorDto;
import com.jonathanfoucher.databaseexample.data.model.Director;
import com.jonathanfoucher.databaseexample.data.repository.DirectorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public DirectorDto findById(Long id) {
        Director director = findByIdOrThrowNotFound(id);
        return convertEntityToDto(director);
    }

    @Transactional
    public List<DirectorDto> findAllByOrderByLastNameAscFirstNameAsc() {
        return directorRepository.findAllByOrderByLastNameAscFirstNameAsc()
                .map(this::convertEntityToDto)
                .toList();
    }

    @Transactional
    public List<DirectorDto> findByLastName(String lastName) {
        return directorRepository.findByLastName(lastName)
                .map(this::convertEntityToDto)
                .toList();
    }

    @Transactional
    public void save(DirectorDto director) {
        Director directorToSave = director.getId() != null ? findByIdOrThrowNotFound(director.getId()) : new Director();
        directorToSave.setFirstName(director.getFirstName());
        directorToSave.setLastName(director.getLastName());
        directorRepository.save(directorToSave);
    }

    @Transactional
    public void deleteById(Long id) {
        Director director = findByIdOrThrowNotFound(id);
        directorRepository.delete(director);
    }

    private Director findByIdOrThrowNotFound(Long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new DirectorNotFoundException(id));
    }

    private DirectorDto convertEntityToDto(Director entity) {
        DirectorDto dto = new DirectorDto();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        return dto;
    }
}
