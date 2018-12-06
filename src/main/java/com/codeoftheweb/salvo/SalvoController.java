package com.codeoftheweb.salvo;

import com.sun.xml.internal.bind.v2.model.core.ID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @RequestMapping("/games")
    public List<Map<String, Object>> getGamesId() {
        return gameRepository.findAll()
                .stream().map(game -> new HashMap<String, Object>(){{
                    put("id", game.getGameId());
                    put("created", game.getDate());
                }}).collect(Collectors.toList());
    }
}