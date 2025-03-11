package com.jonathanfoucher.databaseexample.services;

import com.jonathanfoucher.databaseexample.common.errors.DirectorNotFoundException;
import com.jonathanfoucher.databaseexample.data.dto.DirectorDto;
import com.jonathanfoucher.databaseexample.data.model.Director;
import com.jonathanfoucher.databaseexample.data.repository.DirectorRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(DirectorService.class)
class DirectorServiceTest {
    @Autowired
    private DirectorService directorService;
    @MockitoBean
    private DirectorRepository directorRepository;

    private static final Long ID = 2L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @Test
    void findById() {
        // GIVEN
        Director director = initDirector();

        when(directorRepository.findById(ID))
                .thenReturn(Optional.of(director));

        // WHEN
        DirectorDto result = directorService.findById(ID);

        // THEN
        verify(directorRepository, times(1)).findById(ID);

        checkDirectorDto(result);
    }

    @Test
    void findByIdWithDirectorNotFound() {
        // GIVEN
        when(directorRepository.findById(ID))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> directorService.findById(ID))
                .isInstanceOf(DirectorNotFoundException.class)
                .hasMessage("Director with id 2 not found");

        verify(directorRepository, times(1)).findById(ID);
    }

    @Test
    void findAllByOrderByLastNameAscFirstNameAsc() {
        // GIVEN
        Director director = initDirector();

        when(directorRepository.findAllByOrderByLastNameAscFirstNameAsc())
                .thenReturn(Stream.of(director));

        // WHEN
        List<DirectorDto> results = directorService.findAllByOrderByLastNameAscFirstNameAsc();

        // THEN
        verify(directorRepository, times(1)).findAllByOrderByLastNameAscFirstNameAsc();

        assertNotNull(results);
        assertEquals(1, results.size());

        DirectorDto result = results.getFirst();
        checkDirectorDto(result);
    }

    @Test
    void findAllByOrderByLastNameAscFirstNameAscWithoutResult() {
        // GIVEN
        when(directorRepository.findAllByOrderByLastNameAscFirstNameAsc())
                .thenReturn(Stream.empty());

        // WHEN
        List<DirectorDto> results = directorService.findAllByOrderByLastNameAscFirstNameAsc();

        // THEN
        verify(directorRepository, times(1)).findAllByOrderByLastNameAscFirstNameAsc();

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void findByLastName() {
        // GIVEN
        Director director = initDirector();

        when(directorRepository.findByLastName(LAST_NAME))
                .thenReturn(Stream.of(director));

        // WHEN
        List<DirectorDto> results = directorService.findByLastName(LAST_NAME);

        // THEN
        verify(directorRepository, times(1)).findByLastName(LAST_NAME);

        assertNotNull(results);
        assertEquals(1, results.size());

        DirectorDto result = results.getFirst();
        checkDirectorDto(result);
    }

    @Test
    void findByLastNameWithoutResult() {
        // GIVEN
        when(directorRepository.findByLastName(LAST_NAME))
                .thenReturn(Stream.empty());

        // WHEN
        List<DirectorDto> results = directorService.findByLastName(LAST_NAME);

        // THEN
        verify(directorRepository, times(1)).findByLastName(LAST_NAME);

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void saveDirectorCreated() {
        // GIVEN
        DirectorDto director = initDirectorDto();
        director.setId(null);

        // WHEN
        directorService.save(director);

        // THEN
        ArgumentCaptor<Director> capturedDirector = ArgumentCaptor.forClass(Director.class);
        verify(directorRepository, never()).findById(ID);
        verify(directorRepository, times(1)).save(capturedDirector.capture());

        Director savedDirector = capturedDirector.getValue();
        assertNotNull(savedDirector);
        assertNull(savedDirector.getId());
        assertEquals(FIRST_NAME, savedDirector.getFirstName());
        assertEquals(LAST_NAME, savedDirector.getLastName());
    }

    @Test
    void saveDirectorCreatedUpdated() {
        // GIVEN
        DirectorDto director = initDirectorDto();

        Director dbDirector = initDirector();
        dbDirector.setFirstName(FIRST_NAME + "Previous");

        when(directorRepository.findById(ID))
                .thenReturn(Optional.of(dbDirector));

        // WHEN
        directorService.save(director);

        // THEN
        ArgumentCaptor<Director> capturedDirector = ArgumentCaptor.forClass(Director.class);
        verify(directorRepository, times(1)).findById(ID);
        verify(directorRepository, times(1)).save(capturedDirector.capture());

        Director savedDirector = capturedDirector.getValue();
        assertNotNull(savedDirector);
        assertEquals(ID, savedDirector.getId());
        assertEquals(FIRST_NAME, savedDirector.getFirstName());
        assertEquals(LAST_NAME, savedDirector.getLastName());
    }

    @Test
    void saveDirectorCreatedUpdatedWithDirectorNotFound() {
        // GIVEN
        DirectorDto director = initDirectorDto();

        when(directorRepository.findById(ID))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> directorService.save(director))
                .isInstanceOf(DirectorNotFoundException.class)
                .hasMessage("Director with id 2 not found");

        verify(directorRepository, times(1)).findById(ID);
        verify(directorRepository, never()).save(any());
    }

    @Test
    void deleteById() {
        // GIVEN
        Director director = initDirector();

        when(directorRepository.findById(ID))
                .thenReturn(Optional.of(director));

        // WHEN
        directorService.deleteById(ID);

        // THEN
        ArgumentCaptor<Director> capturedDirector = ArgumentCaptor.forClass(Director.class);
        verify(directorRepository, times(1)).findById(ID);
        verify(directorRepository, times(1)).delete(capturedDirector.capture());

        Director deletedDirector = capturedDirector.getValue();
        assertNotNull(deletedDirector);
        assertEquals(ID, deletedDirector.getId());
        assertEquals(FIRST_NAME, deletedDirector.getFirstName());
        assertEquals(LAST_NAME, deletedDirector.getLastName());
    }

    @Test
    void deleteByIdWithDirectorNotFound() {
        // GIVEN
        when(directorRepository.findById(ID))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> directorService.deleteById(ID))
                .isInstanceOf(DirectorNotFoundException.class)
                .hasMessage("Director with id 2 not found");

        verify(directorRepository, times(1)).findById(ID);
        verify(directorRepository, never()).delete(any(Director.class));
    }

    private Director initDirector() {
        Director director = new Director();
        director.setId(ID);
        director.setFirstName(FIRST_NAME);
        director.setLastName(LAST_NAME);
        director.setUpdatedAt(ZonedDateTime.now().minusDays(1));
        return director;
    }

    private DirectorDto initDirectorDto() {
        DirectorDto director = new DirectorDto();
        director.setId(ID);
        director.setFirstName(FIRST_NAME);
        director.setLastName(LAST_NAME);
        return director;
    }

    private void checkDirectorDto(DirectorDto director) {
        assertNotNull(director);
        assertEquals(ID, director.getId());
        assertEquals(FIRST_NAME, director.getFirstName());
        assertEquals(LAST_NAME, director.getLastName());
    }
}
