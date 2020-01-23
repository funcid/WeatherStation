package ru.func.weathersender.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.func.weathersender.entity.Notation;
import ru.func.weathersender.repository.NotationRepository;
import ru.func.weathersender.util.Location;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author func 23.01.2020
 */
@Slf4j
@RestController
public class ApiController {

    @Autowired
    protected NotationRepository notationRepository;

    private static final String APPLICATION_JSON_VALUE_UTF8 = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8";
    private static final String LOGGER_OUTPUT_MESSAGE = "Свежие записи были оправлены в формате {}. IP получателя {}.";

    @GetMapping(
            headers = HttpHeaders.ACCEPT + "=" + MediaType.APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE_UTF8,
            path = "/byId")
    public Notation sendDataById(HttpServletRequest request, @RequestParam int id) {
        log.info(LOGGER_OUTPUT_MESSAGE, "JSON", request.getRemoteAddr());
        return notationRepository.findById(id)
                .filter(Notation::getIsPublic)
                .orElse(null);
    }

    @GetMapping(
            headers = HttpHeaders.ACCEPT + "=" + MediaType.APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE_UTF8,
            path = "/byLocation")
    public List<Notation> sendDataById(HttpServletRequest request, @RequestParam String location) {
        log.info(LOGGER_OUTPUT_MESSAGE, "JSON", request.getRemoteAddr());
        return notationRepository.findByLocation(location).stream()
                .filter(Notation::getIsPublic)
                .collect(Collectors.toList());
    }

    @RequestMapping(
            headers = HttpHeaders.ACCEPT + "=" + MediaType.APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE_UTF8,
            path = "/byTimestamp")
    public List<Notation> sendDataByTimestamp(HttpServletRequest request, @RequestParam String timestamp) {
        log.info(LOGGER_OUTPUT_MESSAGE, "JSON", request.getRemoteAddr());
        return notationRepository.findByTimestamp(timestamp).stream()
                .filter(Notation::getIsPublic)
                .collect(Collectors.toList());
    }

    private List<Notation> getNotificationList() {
        return Stream.of(Location.values())
                .map(location -> notationRepository.findNewestNotationByLocation(location.getCords()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(Notation::getIsPublic)
                .collect(Collectors.toList());
    }
}