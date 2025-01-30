package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;

    public Collection<Director> getAllDirectors() {
        return directorRepository.findAll();
    }

    public Director getDirectorById(Long id) {
        Director director = directorRepository.findById(id);
        if (director == null) {
            throw new NotFoundException("Режиссер не найден!");
        }
        return director;
    }

    public Director createDirector(Director director) {
        return directorRepository.save(director);
    }

    public Director updateDirector(Director director) {
        if (directorRepository.findById(director.getId()) == null) {
            throw new NotFoundException("Режиссер не найден!");
        }
        return directorRepository.update(director);
    }

    public void deleteDirector(Long id) {
        if (directorRepository.findById(id) == null) {
            throw new NotFoundException("Режиссер не найден!");
        }
        directorRepository.delete(id);
    }
}
