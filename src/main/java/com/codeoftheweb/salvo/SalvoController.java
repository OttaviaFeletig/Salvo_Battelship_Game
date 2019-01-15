
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
    private ShipRepository shipRepository;

    @Autowired
    public SalvoController(GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, PlayerRepository playerRepository, ShipRepository shipRepository) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.playerRepository = playerRepository;
        this.shipRepository = shipRepository;
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

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createNewGame(Authentication authentication){
        if(authentication.getName().isEmpty()){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "You are not logged in"), HttpStatus.UNAUTHORIZED);
        }
        Date newDate = new Date();
        Game newGame = new Game(newDate);
        GamePlayer newGamePlayer = new GamePlayer(newDate);
        Player player = playerRepository.findByEmail(authentication.getName());

        gamePlayerRepository.save(newGamePlayer);
        playerRepository.save(player);
        gameRepository.save(newGame);

        player.addGamePlayer(newGamePlayer);
        newGame.addGamePlayer(newGamePlayer);

        playerRepository.save(player);
        gameRepository.save(newGame);

        return new ResponseEntity<>(makeMapForResponseEntity("gamePlayerId", newGamePlayer.getGamePlayerId()), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gameId, Authentication authentication){
        Game currentGame = gameRepository.findByGameId(gameId);
        Player player = playerRepository.findByEmail(authentication.getName());
        if(authentication.getName().isEmpty()){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "You are not logged in"), HttpStatus.UNAUTHORIZED);
        }
        if(currentGame == null){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "No such game"), HttpStatus.FORBIDDEN);
        }
        if(currentGame.getGamePlayers().size() > 1){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "This game is full"), HttpStatus.FORBIDDEN);
        }
        if(currentGame.getPlayers().contains(player)){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "You are already in this game"), HttpStatus.CONFLICT);
        }
        System.out.println(currentGame.getPlayers().contains(authentication.getName()));
        Date newDate = new Date();
        GamePlayer newGamePlayer = new GamePlayer(newDate);

        gamePlayerRepository.save(newGamePlayer);
        playerRepository.save(player);
        gameRepository.save(currentGame);
        player.addGamePlayer(newGamePlayer);
        currentGame.addGamePlayer(newGamePlayer);
        playerRepository.save(player);
        gameRepository.save(currentGame);

        return new ResponseEntity<>(makeMapForResponseEntity("gamePlayerId", newGamePlayer.getGamePlayerId()), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getShipLocation(@PathVariable Long gamePlayerId, Authentication authentication, @RequestBody List<Ship> shipList){
        GamePlayer currentGamePlayer = gamePlayerRepository.findByGamePlayerId(gamePlayerId);
//        Game currentGame = currentGamePlayer.getGame();
        Integer currentGameShips = currentGamePlayer.getShipTypes().size();
        if(authentication.getName().isEmpty()){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The player is not logged in"), HttpStatus.UNAUTHORIZED);
        }
        if(currentGamePlayer == null){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "There is not current game player"), HttpStatus.UNAUTHORIZED);
        }
        if(!getLoggedInGamePlayer(authentication, currentGamePlayer)){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The current game player is not the logged in player"), HttpStatus.UNAUTHORIZED);
        }
        if(currentGameShips > 0){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The ships have already been added"), HttpStatus.FORBIDDEN);
        }
        shipList.forEach(ship -> {

//            String newShipType = new String(ship.getShipType());
//            List<String> newShipLocations = new ArrayList<>(ship.getShipLocations());
//            Ship newShip = new Ship(newShipType, newShipLocations);
//            gamePlayerRepository.save(currentGamePlayer);
            shipRepository.save(ship);
            currentGamePlayer.addShipTypes(ship);
//            gamePlayerRepository.save(currentGamePlayer);
        });

        return new ResponseEntity<>(makeMapForResponseEntity("success", "The ships have been added successfully"), HttpStatus.CREATED);
    }

    private List<Ship> makeListForShipResponseEntity(Ship ship){
        List<Ship> listOfShips = new ArrayList<>();
        listOfShips.add(ship);
        return listOfShips;
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
            return new ResponseEntity<>(makeMapForResponseEntity("error", "No email given"), HttpStatus.FORBIDDEN);
        }
        Player player = playerRepository.findByEmail(email);
        if(player != null){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "Email already used"), HttpStatus.CONFLICT);
        }
        Player newPlayer = playerRepository.save(new Player(email, password, name));
        return new ResponseEntity<>(makeMapForResponseEntity("email", newPlayer.getEmail()), HttpStatus.CREATED);
    }

    private HashMap<String, Object> makeMapForResponseEntity(String key, Object value){
        HashMap<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

}