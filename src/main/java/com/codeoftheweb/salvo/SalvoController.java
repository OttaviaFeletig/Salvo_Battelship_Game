package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
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
    public List<HashMap<String, Object>> getGamesId() {
        return gameRepository.findAll()
                .stream().map(game -> new LinkedHashMap<String, Object>(){{
                    put("id", game.getGameId());
                    put("created", game.getDate());
                    put("gamePlayers", game.getGamePlayers()
                            .stream()
                            .map(gamePlayer -> new LinkedHashMap<String, Object>(){{
                                put("id", gamePlayer.getGamePlayerId());
                                put("player", getHashPlayer(gamePlayer.getPlayer()));
                            }}).collect(toList())
                    );
                }}).collect(Collectors.toList());
    }

    private HashMap<String, Object> getHashPlayer(Player player){
        return new LinkedHashMap<String, Object>() {{
            put("id", player.getPlayerId());
            put("email", player.getUserName());
        }};
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    private HashMap<String, Object> getOneGame(@PathVariable Long gamePlayerId){
        GamePlayer gamePlayer = gamePlayerRepository.findOne(gamePlayerId);

        return new LinkedHashMap<String, Object>(){{
            put("id", gamePlayer.getGame().getGameId());
            put("created", gamePlayer.getGame().getDate());
            put("gamePlayers", getGamePlayers(gamePlayer.getGame()));
            put("ships", getGamePlayerShipType(gamePlayer));
        }};
    }

    private List<HashMap<String, Object>> getGamePlayers (Game game){
        return game.getGamePlayers()
                .stream()
                .map(gamePlayer -> new LinkedHashMap<String, Object>(){{
                    put("id", gamePlayer.getGamePlayerId());
                    put("player", getHashPlayer(gamePlayer.getPlayer()));
                }}).collect(toList());
    }

    private List<HashMap<String, Object>> getGamePlayerShipType(GamePlayer gamePlayer){
        return gamePlayer.getShipTypes()
                .stream()
                .map(ship -> new LinkedHashMap<String, Object>(){{
                    put("type", ship.getShipType());
                    put("locations", ship.getShipLocations());
                }}).collect(Collectors.toList());
    }

}