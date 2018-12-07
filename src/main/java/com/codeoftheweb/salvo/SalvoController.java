package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    put("gamePlayers", game.getGamePlayers()
                            .stream()
                            .map(gamePlayer -> new HashMap<String, Object>(){{
                                put("id", gamePlayer.getGamePlayerId());
                                put("player", new HashMap<String, Object>(){{
                                    put("id", gamePlayer.getPlayer().getPlayerId());
                                    put("email", gamePlayer.getPlayer().getUserName());
                                }});
                            }}).collect(toList())
                    );
                }}).collect(Collectors.toList());
    }
}