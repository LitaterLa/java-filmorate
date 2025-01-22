package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpaa;
import ru.yandex.practicum.filmorate.service.MpaaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
public class MpaaController {
    private final MpaaService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Mpaa> getMpaa() {
        List<Mpaa> rating = service.getMpaa();
        log.info("получение всех рейтингов");
        return service.getMpaa();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mpaa getById(@PathVariable int id) {
        Mpaa mpaa = service.getById(id);
        log.info("получение рейтига по ид={}", id);
        return service.getById(id);
    }
}
