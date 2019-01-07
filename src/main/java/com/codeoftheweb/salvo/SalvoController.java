package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private PlayerRepository playerRepository;

    @Autowired
    public SalvoController(GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.playerRepository = playerRepository;
    }

    @RequestMapping("/leader_board")
    public List<HashMap<String, Object>> getPlayersScore(){
        return playerRepository.findAll()
                .stream()
                .map(player -> new LinkedHashMap<String, Object>(){{
                    put("player", player.getName());
                    put("scores", player.getScores()
                            .stream()
                            .map(score -> score.getScore()).collect(toList()));
                }}).collect(toList());
    }

    @RequestMapping("/games")
    public HashMap<String, Object> getGames(Authentication authentication){
        if(!(isGuest(authentication))){
            return new LinkedHashMap<String, Object>(){{
                put("player", getHashPlayer(getLoggedInPlayer(authentication)));
                put("games", getAllGames());
            }};
        }else{
            return new LinkedHashMap<String, Object>(){{
                put("player", null);
                put("games", getAllGames());
            }};
        }
    }
    private List<HashMap<String, Object>> getAllGames() {
        return gameRepository.findAll()
                .stream().map(game -> new LinkedHashMap<String, Object>(){{
                    put("id", game.getGameId());
                    put("created", game.getDate());
                    put("finished", getScoreDate(game.getScores()));
                    put("gamePlayers", game.getGamePlayers()
                            .stream()
                            .map(gamePlayer -> new LinkedHashMap<String, Object>(){{
                                put("id", gamePlayer.getGamePlayerId());
                                put("player", getHashPlayer(gamePlayer.getPlayer()));
                                put("score", gamePlayer.getScoreInGame(game));
                            }}).collect(toList())
                    );
                }}).collect(Collectors.toList());
    }

    private Date getScoreDate(Set<Score> scores){
        return scores.stream()
                .findFirst()
                .map(score -> score.getFinishDate())
                .orElse(null);
    }

    private HashMap<String, Object> getHashPlayer(Player player){
        return new LinkedHashMap<String, Object>() {{
            put("id", player.getPlayerId());
            put("name", player.getName());
            put("email", player.getUserName());
        }};
    }

    private Player getLoggedInPlayer(Authentication authentication){
        return playerRepository.findByUserName(authentication.getName());
    }

    private boolean isGuest(Authentication authentication){
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
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
                    put("location", ship.getShipLocations());
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