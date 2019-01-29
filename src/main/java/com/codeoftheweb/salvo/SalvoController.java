
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
    private SalvoRepository salvoRepository;

    @Autowired
    public SalvoController(GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, PlayerRepository playerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.playerRepository = playerRepository;
        this.shipRepository = shipRepository;
        this.salvoRepository = salvoRepository;
    }

    @RequestMapping("/leader_board")
    public List<HashMap<String, Object>> getPlayersScore() {
        return playerRepository.findAll()
                .stream()
                .map(player -> new LinkedHashMap<String, Object>() {{
                    put("player", player.getName());
                    put("scores", player.getScores()
                            .stream()
                            .map(score -> score.getScore()).collect(toList()));
                }}).collect(toList());
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createNewGame(Authentication authentication) {
        if (authentication.getName().isEmpty()) {
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
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gameId, Authentication authentication) {
        Game currentGame = gameRepository.findByGameId(gameId);
        Player player = playerRepository.findByEmail(authentication.getName());
        if (authentication.getName().isEmpty()) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "You are not logged in"), HttpStatus.UNAUTHORIZED);
        }
        if (currentGame == null) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "No such game"), HttpStatus.FORBIDDEN);
        }
        if (currentGame.getGamePlayers().size() > 1) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "This game is full"), HttpStatus.FORBIDDEN);
        }
        if (currentGame.getPlayers().contains(player)) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "You are already in this game"), HttpStatus.CONFLICT);
        }
//        System.out.println(currentGame.getPlayers().contains(authentication.getName()));
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
    public ResponseEntity<Map<String, Object>> getShipLocation(@PathVariable Long gamePlayerId, Authentication authentication, @RequestBody List<Ship> shipList) {
        GamePlayer currentGamePlayer = gamePlayerRepository.findByGamePlayerId(gamePlayerId);
        Integer currentGameShips = currentGamePlayer.getShipTypes().size();
        if (authentication.getName().isEmpty()) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The player is not logged in"), HttpStatus.UNAUTHORIZED);
        }
        if (currentGamePlayer == null) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "There is not current game player"), HttpStatus.UNAUTHORIZED);
        }
        if (!getLoggedInGamePlayer(authentication, currentGamePlayer)) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The current game player is not the logged in player"), HttpStatus.UNAUTHORIZED);
        }
        if (currentGameShips > 0) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The ships have already been added"), HttpStatus.FORBIDDEN);
        }
        shipList.forEach(ship -> {
//            System.out.println(currentGamePlayer);
            currentGamePlayer.addShipTypes(ship);
            shipRepository.save(ship);
        });

        return new ResponseEntity<>(makeMapForResponseEntity("success", "The ships have been added successfully"), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/salvos", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getSalvoLocation(@PathVariable Long gamePlayerId, Authentication authentication, @RequestBody Salvo currentSalvo) {
        GamePlayer currentGamePlayer = gamePlayerRepository.findByGamePlayerId(gamePlayerId);

        if (authentication.getName().isEmpty()) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The player is not logged in"), HttpStatus.UNAUTHORIZED);
        }
        if (currentGamePlayer == null) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "There is not current game player"), HttpStatus.UNAUTHORIZED);
        }
        if (!getLoggedInGamePlayer(authentication, currentGamePlayer)) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The current game player is not the logged in player"), HttpStatus.UNAUTHORIZED);
        }
        if (checkIfSalvoHasBeenAlreadyFired(currentGamePlayer, currentSalvo)) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The salvos have already been added"), HttpStatus.FORBIDDEN);
        }
        currentGamePlayer.addSalvos(currentSalvo);
        salvoRepository.save(currentSalvo);

        return new ResponseEntity<>(makeMapForResponseEntity("success", "The salvos have been added successfully"), HttpStatus.CREATED);
    }

    private boolean checkIfSalvoHasBeenAlreadyFired(GamePlayer currentGamePlayer, Salvo currentSalvo) {
        if (currentGamePlayer.getSalvos().size() > 0) {
            currentGamePlayer.getSalvos().stream().map(salvo -> {
                if (salvo.getTurnNumber() == currentSalvo.getTurnNumber()) {
                    return true;
                }
                return false;
            });
        }
        return false;
    }

    @RequestMapping("/games")
    public HashMap<String, Object> getGames(Authentication authentication) {
        if (!(isGuest(authentication))) {
            return new LinkedHashMap<String, Object>() {{
                put("player", getHashPlayer(getLoggedInPlayer(authentication)));
                put("games", getAllGames());
            }};
        } else {
            return new LinkedHashMap<String, Object>() {{
                put("player", null);
                put("games", getAllGames());
            }};
        }
    }

    private List<HashMap<String, Object>> getAllGames() {
        return gameRepository.findAll()
                .stream().map(game -> new LinkedHashMap<String, Object>() {{
                    put("id", game.getGameId());
                    put("created", game.getDate());
                    put("finished", getScoreDate(game.getScores()));
                    put("gamePlayers", game.getGamePlayers()
                            .stream()
                            .map(gamePlayer -> new LinkedHashMap<String, Object>() {{
                                put("id", gamePlayer.getGamePlayerId());
                                put("player", getHashPlayer(gamePlayer.getPlayer()));
                                put("score", gamePlayer.getScoreInGame(game));
                            }}).collect(toList())
                    );
                }}).collect(Collectors.toList());
    }

    private Date getScoreDate(Set<Score> scores) {
        return scores.stream()
                .findFirst()
                .map(score -> score.getFinishDate())
                .orElse(null);
    }

    private HashMap<String, Object> getHashPlayer(Player player) {
        return new LinkedHashMap<String, Object>() {{
            put("id", player.getPlayerId());
            put("name", player.getName());
            put("email", player.getEmail());
        }};
    }

    private Player getLoggedInPlayer(Authentication authentication) {
        return playerRepository.findByEmail(authentication.getName());
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    private Object gameView(@PathVariable Long gamePlayerId, Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.findByGamePlayerId(gamePlayerId);
        if (getLoggedInGamePlayer(authentication, gamePlayer)) {
            return getOneGame(gamePlayer);
        } else {
            return unauthorizedGameView();
        }
    }

    private boolean getLoggedInGamePlayer(Authentication authentication, GamePlayer gamePlayer) {
        if (authentication.getName() == gamePlayer.getPlayer().getEmail()) {
            return true;
        }
        return false;
    }

    private Map<String, Object> getOneGame(GamePlayer gamePlayer) {

        return new LinkedHashMap<String, Object>() {{
            put("id", gamePlayer.getGame().getGameId());
            put("created", gamePlayer.getGame().getDate());
            put("gamePlayers", getGamePlayers(gamePlayer.getGame()));
            put("ships", getGamePlayerShipType(gamePlayer));
            put("salvos", getGameSalvos(gamePlayer.getGame().getGamePlayers()));
            put("hitAndSunk", getHitAndSunk(gamePlayer));
        }};
    }

    private Map<String, Object> getHitAndSunk(GamePlayer gamePlayer) {
        Set<Salvo> gamePlayerSalvos = gamePlayer.getSalvos();
        Set<Ship> gamePlayerShips = gamePlayer.getShipTypes();
        GamePlayer opponentGamePlayer = findOpponentGamePlayer(gamePlayer);
//        System.out.println(opponentGamePlayer.getPlayer().getEmail());
        Set<Ship> opponentShips = opponentGamePlayer.getShipTypes();
        Set<Salvo> opponentSalvos = opponentGamePlayer.getSalvos();
//        System.out.println(opponentShips);
        Map<String, Object> hitAndSunkMap = new LinkedHashMap<>();


        hitAndSunkMap.put("gamePlayerId", gamePlayer.getGamePlayerId());
        hitAndSunkMap.put("opponentHitAndSunk", getOneSalvo(gamePlayerSalvos, opponentShips));
        hitAndSunkMap.put("opponentPlayerId", opponentGamePlayer.getGamePlayerId());
        hitAndSunkMap.put("myHitAndSunk", getOneSalvo(opponentSalvos, gamePlayerShips));


        return hitAndSunkMap;
    }

    private List<Map<String, Object>> getOneSalvo(Set<Salvo> gamePlayerSalvos, Set<Ship> opponentShips){
//        System.out.println(opponentShips);
        return gamePlayerSalvos.stream().map(salvo -> new LinkedHashMap<String, Object>(){{

            put("turnNumber", salvo.getTurnNumber());
            put("hits", getHit(salvo, opponentShips));
        }}).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getHit(Salvo gamePlayerSalvo, Set<Ship> opponentShips){
//        System.out.println(opponentShips);

        List<Map<String, Object>> hitList = new ArrayList<>();

        opponentShips.forEach(ship -> {
            Map<String, Object> hitMap = new LinkedHashMap<>();

//            System.out.println(ship);
//            System.out.println(gamePlayerSalvo.getSalvoLocations());
            if(getOneSalvoLocation(gamePlayerSalvo.getSalvoLocations(), ship).isEmpty()){
                hitMap.put("shipType", null);
                hitMap.put("location", null);
                hitMap.put("damage", null);
            }else{
                hitMap.put("shipType", ship.getShipType());
                hitMap.put("location", getOneSalvoLocation(gamePlayerSalvo.getSalvoLocations(), ship));
                hitMap.put("damage", getDamage(getOneSalvoLocation(gamePlayerSalvo.getSalvoLocations(), ship)));
            }
            hitList.add(hitMap);
            System.out.println(hitMap);
            System.out.println(hitList);
        });
//        System.out.println(hitList);
        return hitList;
    }

    private List<String> getOneSalvoLocation(List gamePlayerSalvoLocations, Ship opponentShip){
        List<String> hitLocation = new ArrayList<>();
        for (int i = 0; i < gamePlayerSalvoLocations.size(); i++){
            if(opponentShip.getShipLocations().contains(gamePlayerSalvoLocations.get(i))){
                hitLocation.add(gamePlayerSalvoLocations.get(i).toString());
            }
        }
        System.out.println(hitLocation);
        return hitLocation;
    }

    private Integer getDamage(List locations){
        return locations.size();
    }

    private GamePlayer findOpponentGamePlayer(GamePlayer gamePlayer){
        Map<String, GamePlayer> helperMap = new HashMap<>();
        gamePlayer.getGame().getGamePlayers()
                .stream()
                .forEach(gp -> {
                    if(gp.getGamePlayerId() != gamePlayer.getGamePlayerId()){
                        helperMap.put("opponent", gp);
                    }
                });
        return helperMap.get("opponent");
    }

//    private Set<Salvo> getGamePlayerSalvo(GamePlayer gamePlayer){
//        return gamePlayer.getSalvos();
//    }


    private ResponseEntity<String> unauthorizedGameView() {
        return new ResponseEntity<>("You are not authorized", HttpStatus.UNAUTHORIZED);
    }

    private List<Map<String, Object>> getGamePlayers(Game game) {
        return game.getGamePlayers()
                .stream()
                .map(gamePlayer -> new LinkedHashMap<String, Object>() {{
                    put("id", gamePlayer.getGamePlayerId());
                    put("player", getHashPlayer(gamePlayer.getPlayer()));
                }}).collect(toList());
    }

    private List<Map<String, Object>> getGamePlayerShipType(GamePlayer gamePlayers) {
//        System.out.println(gamePlayers);
//        System.out.println(gamePlayers.getShipTypes());
        return gamePlayers.getShipTypes()
                .stream()
                .map(ship -> new LinkedHashMap<String, Object>() {{
                    put("type", ship.getShipType());
                    put("location", ship.getShipLocations());
                }}).collect(toList());
    }

    private List<HashMap<String, Object>> getGameSalvos(Set<GamePlayer> gamePlayer) {
        return gamePlayer
                .stream()
                .flatMap(oneGamePlayer -> oneGamePlayer.getSalvos()
                        .stream()
                        .map(salvo -> new LinkedHashMap<String, Object>() {{
                            put("gamePlayerId", salvo.getGamePlayer().getGamePlayerId());
                            put("turnNumber", salvo.getTurnNumber());
                            put("location", salvo.getSalvoLocations());
                        }})).collect(toList());
    }

//    private List<Object> getGameHits(Set<Ship> shipSet){
//        Set<Salvo> salvoSet =
////        return salvoSet.stream().map(salvo -> checkIfItIsAHit(salvo)).collect(Collectors.toList());
////        return gamePlayerSet
////                .stream()
////                .map(gamePlayer -> new LinkedHashMap<String, Object>(){{
////                    put("gamePlayerId", gamePlayer.getGamePlayerId());
////                    put("turnNumber", gamePlayer.getSalvos().stream().map(salvo -> salvo.getTurnNumber()));
//////                    put("ship", checkIfItIsAHit(gamePlayerSet));
//////                    put("shipType", gamePlayer.getShipTypes().stream().map(ship -> ship.getShipType()));
////                }}).collect(toList());
//    }


//    private Object checkIfItIsAHit(Salvo salvo){
//        List<String> gameSalvoLocation = getGameSalvoLocation(salvo);
//        List<String> gameShipLocation = getGameShipLocation(salvo);
////        Object hitShip;
//        return gameShipLocation
//                .stream()
//                .map(ship -> {
//            if(gameSalvoLocation.contains(ship)){
//                Object hitShip = shipRepository.findAll().stream().map(ship1 -> {
//                    if(ship1.getShipLocations().contains(ship)){
//                        System.out.println(ship1);
//                        return ship1;
//                    }
//                    return null;
//                });
//                        return hitShip;
//            }
//            return null;
//        });
////        if(getGameSalvoLocation(gamePlayerSet).size() > 0){
////            getGameSalvoLocation(gamePlayerSet).stream().map(salvo -> {
////                if(getGameShipLocation(gamePlayerSet).contains(salvo)){
////                    System.out.println("hit!");
////                    return true;
////                }
////                return false;
////            });
////        }
////        return false;
//
//    }

//    private List<String> getGameSalvoLocation(Salvo salvo){
//        return salvo.getSalvoLocations();
//    }
//
//    private List<String> getGameShipLocation(Salvo salvo){
//        return salvo.getGamePlayer().getShipTypes()
//                .stream()
//                .flatMap(ship -> ship.getShipLocations()).collect(toList());
//
//    }


    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<HashMap<String, Object>> createPlayer(@RequestParam("email") String email, @RequestParam("password") String password, @RequestParam("name") String name) {
        if (email.isEmpty()) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "No email given"), HttpStatus.FORBIDDEN);
        }
        Player player = playerRepository.findByEmail(email);
        if (player != null) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "Email already used"), HttpStatus.CONFLICT);
        }
        Player newPlayer = playerRepository.save(new Player(email, password, name));
        return new ResponseEntity<>(makeMapForResponseEntity("email", newPlayer.getEmail()), HttpStatus.CREATED);
    }

    private HashMap<String, Object> makeMapForResponseEntity(String key, Object value) {
        HashMap<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

}