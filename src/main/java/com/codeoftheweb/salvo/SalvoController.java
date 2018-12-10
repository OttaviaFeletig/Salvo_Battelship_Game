package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class SalvoController {

    private GameRepository gameRepository;
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    public SalvoController(GameRepository gameRepository, GamePlayerRepository gamePlayerRepository) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
    }

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
                                put("player", getHashPlayer(gamePlayer.getPlayer()));
                            }}).collect(toList())
                    );
                }}).collect(Collectors.toList());
    }

    private Map<String, Object> getHashPlayer(Player player){
        return new HashMap<String, Object>() {{
            put("id", player.getPlayerId());
            put("email", player.getUserName());
        }};
    }

    @RequestMapping("/game_view/{urlGamePlayerId}")
    public Map<String, Object> getOneGame(@PathVariable Long urlGamePlayerId){
        GamePlayer gamePlayerId = gamePlayerRepository.findOne(urlGamePlayerId);

        return new HashMap<String, Object>(){{
            put("id", gamePlayerId.getGame().getGameId());
            put("created", gamePlayerId.getGame().getDate());
            put("gamePlayers", new ArrayList<Map<String, Object>>(){{
                add(new HashMap<String, Object>(){{
                    gamePlayerId.getGame().getGamePlayers()
                            .stream().forEach(gamePlayer -> {
                                put("id", gamePlayer.getGamePlayerId());
                                put("player", getHashPlayer(gamePlayer.getPlayer()));
                    });
                }});
            }});
        }};
    }

}