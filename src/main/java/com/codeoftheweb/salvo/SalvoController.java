package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
            put("salvos", getGameSalvos(gamePlayer.getGame().getGamePlayers()));
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

    private List<HashMap<String, Object>> getGamePlayerShipType(GamePlayer gamePlayers){
        return gamePlayers.getShipTypes()
                .stream()
                .map(ship -> new LinkedHashMap<String, Object>(){{
                    put("type", ship.getShipType());
                    put("locations", ship.getShipLocations());
                }}).collect(Collectors.toList());
    }

    private List<HashMap<String, Object>> getGameSalvos(Set<GamePlayer> gamePlayer){
        return gamePlayer
                .stream()
                .flatMap(oneGamePlayer -> oneGamePlayer.getSalvos()
                        .stream()
                        .map(salvo -> new LinkedHashMap<String, Object>(){{
                            put("gamePlayerId", salvo.getGamePlayer().getGamePlayerId());
                            put("turnNumber", salvo.getTurnNumber());
                            put("location", salvo.getSalvoLocations());
                        }})).collect(toList());
    }

}