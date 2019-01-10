package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
            put("email", player.getEmail());
        }};
    }

    private Player getLoggedInPlayer(Authentication authentication){
        return playerRepository.findByEmail(authentication.getName());
    }

    private boolean isGuest(Authentication authentication){
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    private Object gameView(@PathVariable Long gamePlayerId, Authentication authentication){
        GamePlayer gamePlayer = gamePlayerRepository.findOne(gamePlayerId);
        if(getLoggedInGamePlayer(authentication, gamePlayer)){
            return getOneGame(gamePlayer);
        }else{
            return unauthorizedGameView();
        }
    }

    private boolean getLoggedInGamePlayer(Authentication authentication, GamePlayer gamePlayer){
        if(authentication.getName() == gamePlayer.getPlayer().getEmail()){
            return true;
        }
        return false;
    }
    private Map<String, Object> getOneGame(GamePlayer gamePlayer){
            return new LinkedHashMap<String, Object>(){{
                put("id", gamePlayer.getGame().getGameId());
                put("created", gamePlayer.getGame().getDate());
                put("gamePlayers", getGamePlayers(gamePlayer.getGame()));
                put("ships", getGamePlayerShipType(gamePlayer));
                put("salvos", getGameSalvos(gamePlayer.getGame().getGamePlayers()));
            }};
    }

    private ResponseEntity<String> unauthorizedGameView(){
        return new ResponseEntity<>("You are not authorized", HttpStatus.UNAUTHORIZED);
    }

    private List<Map<String, Object>> getGamePlayers (Game game){
        return game.getGamePlayers()
                .stream()
                .map(gamePlayer -> new LinkedHashMap<String, Object>(){{
                    put("id", gamePlayer.getGamePlayerId());
                    put("player", getHashPlayer(gamePlayer.getPlayer()));
                }}).collect(toList());
    }

    private List<Map<String, Object>> getGamePlayerShipType(GamePlayer gamePlayers){
        return gamePlayers.getShipTypes()
                .stream()
                .map(ship -> new LinkedHashMap<String, Object>(){{
                    put("type", ship.getShipType());
                    put("location", ship.getShipLocations());
                }}).collect(toList());
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

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<HashMap<String, Object>> createPlayer(@RequestParam("email") String email, @RequestParam("password") String password, @RequestParam("name") String name){
        if(email.isEmpty()){
            return new ResponseEntity<>(makeMap("error", "No email given"), HttpStatus.FORBIDDEN);
        }
        Player player = playerRepository.findByEmail(email);
        if(player != null){
            return new ResponseEntity<>(makeMap("error", "Email already used"), HttpStatus.CONFLICT);
        }
        Player newPlayer = playerRepository.save(new Player(email, password, name));
        return new ResponseEntity<>(makeMap("email", newPlayer.getEmail()), HttpStatus.CREATED);
    }

    private HashMap<String, Object> makeMap(String key, Object value){
        HashMap<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

}