
package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@RestController
@RequestMapping("/api")
public class SalvoController {

    private GameRepository gameRepository;
    private GamePlayerRepository gamePlayerRepository;
    private PlayerRepository playerRepository;
    private ShipRepository shipRepository;
    private SalvoRepository salvoRepository;
    private ScoreRepository scoreRepository;
    private boolean methodCall = false;

    @Autowired
    public SalvoController(GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, PlayerRepository playerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.playerRepository = playerRepository;
        this.shipRepository = shipRepository;
        this.salvoRepository = salvoRepository;
        this.scoreRepository = scoreRepository;
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
        Set<GamePlayer> gamePlayerSet = currentGamePlayer.getGame().getGamePlayers();
        GamePlayer opponentGamePlayer = findOpponentGamePlayer(currentGamePlayer);
//        Map<String, Object> hitAndSunkMap = (Map<String, Object>) getOneGame(currentGamePlayer).get("hitAndSunk");
        if (authentication.getName().isEmpty()) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The player is not logged in"), HttpStatus.UNAUTHORIZED);
        }
        if (currentGamePlayer == null) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "There is not current game player"), HttpStatus.UNAUTHORIZED);
        }
        if (!getLoggedInGamePlayer(authentication, currentGamePlayer)) {
            return new ResponseEntity<>(makeMapForResponseEntity("error", "The current game player is not the logged in player"), HttpStatus.UNAUTHORIZED);
        }
        if(opponentGamePlayer == null){
            return new ResponseEntity<>(makeMapForResponseEntity("error", "You have no opponent"), HttpStatus.SERVICE_UNAVAILABLE);
        }else{
            if (checkIfSalvoHasBeenAlreadyFired(currentGamePlayer, currentSalvo)) {
                return new ResponseEntity<>(makeMapForResponseEntity("error", "The salvos have already been added"), HttpStatus.FORBIDDEN);
            }
            if(checkTurn(gamePlayerSet, currentGamePlayer).get("myLastTurn") > checkTurn(gamePlayerSet, currentGamePlayer).get("opponentLastTurn")){
                return new ResponseEntity<>(makeMapForResponseEntity("error", "You have to wait"), HttpStatus.NOT_ACCEPTABLE);
            }
            if(opponentGamePlayer.getShipTypes().isEmpty()){
                return new ResponseEntity<>(makeMapForResponseEntity("error", "Your have to wait"), HttpStatus.CONFLICT);
            }
            if(methodCall == true){
                return new ResponseEntity<>(makeMapForResponseEntity("error", "The game is finished"), HttpStatus.METHOD_NOT_ALLOWED);
            }
        }
        currentGamePlayer.addSalvos(currentSalvo);
        salvoRepository.save(currentSalvo);
        return new ResponseEntity<>(makeMapForResponseEntity("success", "The salvos have been added successfully"), HttpStatus.CREATED);
    }

    private boolean checkIfSalvoHasBeenAlreadyFired(GamePlayer currentGamePlayer, Salvo currentSalvo) {
        List<Integer> gamePlayerTurn = currentGamePlayer.getSalvos().stream().map(salvo -> salvo.getTurnNumber()).collect(toList());
        if(gamePlayerTurn.contains(currentSalvo.getTurnNumber())){
            return true;
        }
        return false;
    }

    @RequestMapping("/games")
    public Map<String, Object> getGames(Authentication authentication) {
        Map<String, Object> gamesMap = new LinkedHashMap<>();
        if (!(isGuest(authentication))) {
            gamesMap.put("player", getHashPlayer(getLoggedInPlayer(authentication)));
            gamesMap.put("games", getAllGames());
        } else {
            gamesMap.put("player", null);
            gamesMap.put("games", getAllGames());
        }
        return gamesMap;
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
        Map<String, Object> oneGameMap = new LinkedHashMap<>();
        oneGameMap.put("id", gamePlayer.getGame().getGameId());
        oneGameMap.put("created", gamePlayer.getGame().getDate());
        oneGameMap.put("gamePlayers", getGamePlayers(gamePlayer.getGame()));
        oneGameMap.put("ships", getGamePlayerShipType(gamePlayer));
        oneGameMap.put("salvos", getGameSalvos(gamePlayer.getGame().getGamePlayers()));
        oneGameMap.put("hitAndSunk", getHitAndSunk(gamePlayer));
        oneGameMap.put("checkTurn", checkTurn(gamePlayer.getGame().getGamePlayers(), gamePlayer));
        oneGameMap.put("winner", getWinner((Map) oneGameMap.get("hitAndSunk"),gamePlayer));
        return oneGameMap;
    }

    private Object getWinner(Map hitAndSunkMap, GamePlayer gamePlayer){
        List<Map<String, Object>> opponentHitAndSunk = (List<Map<String, Object>>) hitAndSunkMap.get("opponentHitAndSunk");
        List<Map<String, Object>> myHitAndSunk = (List<Map<String, Object>>) hitAndSunkMap.get("myHitAndSunk");
//        System.out.println(myHitAndSunk);
//        System.out.println(opponentHitAndSunk);
        Map<String, Object> winner = new HashMap<>();
        GamePlayer opponentGamePlayer = findOpponentGamePlayer(gamePlayer);

        if(hitAndSunkMap.isEmpty()){
            return winner.get("winner");
        }

        Object looser = checkIfGameIsOver(opponentHitAndSunk, myHitAndSunk, gamePlayer);
        System.out.println(looser);

        if(looser == null){
            System.out.println("game is not over");
            winner.put("winner", getGameIsNotOverHelper());
            return winner.get("winner");
        }

        Date finishDate = new Date();
        System.out.println("game is over");

        if(looser == "tie"){
            System.out.println("is a tie");
            winner.put("winner", getTieHelper(gamePlayer, opponentGamePlayer, finishDate));
            return winner.get("winner");
        }
//        Map<String, Object> looserMap = (Map<String, Object>) looser;
        System.out.println("is not a tie");
        System.out.println(looser);
        if(gamePlayer.getGamePlayerId() == (long) looser){
            winner.put("winner" ,getWinnerHelper(opponentGamePlayer, finishDate));
//                        getLooser(gamePlayer, finishDate);
        }else{
            winner.put("winner", getWinnerHelper(gamePlayer, finishDate));
//                        getLooser(opponentGamePlayer, finishDate);
        }

        return winner.get("winner");
    }

    private void getLooser(GamePlayer looserGamePlayer, Date finishDate){
        Score looserScore = new Score(finishDate, 0.0);
        scoreRepository.save(looserScore);
        looserGamePlayer.getPlayer().addScore(looserScore);
    }

    private String getWinnerHelper(GamePlayer winnerGamePlayer, Date finishDate){
        methodCall = true;
        Score winnerScore = new Score(finishDate, 1.0);
        Score looserScore = new Score(finishDate, 0.0);
        findOpponentGamePlayer(winnerGamePlayer).getPlayer().addScore(looserScore);
        findOpponentGamePlayer(winnerGamePlayer).getGame().addScore(looserScore);
        winnerGamePlayer.getPlayer().addScore(winnerScore);
        winnerGamePlayer.getGame().addScore(winnerScore);
        scoreRepository.save(winnerScore);
        scoreRepository.save(looserScore);

        System.out.println(winnerGamePlayer.getPlayer().getName() + winnerGamePlayer.getPlayer().getScores());
        System.out.println(findOpponentGamePlayer(winnerGamePlayer).getPlayer().getName() + findOpponentGamePlayer(winnerGamePlayer).getPlayer().getScores());
        return winnerGamePlayer.getPlayer().getName();
    }

    private List<String> getTieHelper(GamePlayer gamePlayer, GamePlayer opponentGamePlayer, Date finishDate){
        methodCall = true;
        List<String> tieMatch = new ArrayList<>();
        tieMatch.add(gamePlayer.getPlayer().getName());
        tieMatch.add(opponentGamePlayer.getPlayer().getName());
        return tieMatch;
    }

    private Object getGameIsNotOverHelper(){
        methodCall = false;
        return null;
    }

    private Object checkIfGameIsOver(List<Map<String, Object>> opponentHitAndSunk, List<Map<String, Object>> myHitAndSunk, GamePlayer gamePlayer){
//        Map<String, Object> gameIsOver = new HashMap<>();
        if(opponentHitAndSunk.isEmpty() && myHitAndSunk.isEmpty()) {
//            gameIsOver.put("looser", null);
//            return gameIsOver.get("looser");
            return null;
        }

        List<Map<String, Object>> opponentGameIsOver = opponentHitAndSunk
                .stream()
                .filter(oneHit -> (boolean) oneHit.get("gameIsOver") == true)
                .collect(toList());
        System.out.println(opponentGameIsOver);

        List<Map<String, Object>> myGameIsOver = myHitAndSunk
                .stream()
                .filter(oneHit -> (boolean) oneHit.get("gameIsOver") == true)
                .collect(Collectors.toList());
        System.out.println(myGameIsOver);

        Map<String, Integer> checkTurnMap = checkTurn(gamePlayer.getGame().getGamePlayers(), gamePlayer);
        if(checkTurnMap.get("opponentLastTurn") != checkTurnMap.get("myLastTurn")){
            return null;
        }

        if(!opponentGameIsOver.isEmpty() && !myGameIsOver.isEmpty()){
            return "tie";
//            gameIsOver.put("looser", "tie");
        }else if(!opponentGameIsOver.isEmpty() && myGameIsOver.isEmpty()){
            return findOpponentGamePlayer(gamePlayer).getGamePlayerId();
//            gameIsOver.put("looser", opponentHitAndSunk.get(opponentHitAndSunk.size() - 1).get("gamePlayerId"));
        }else if(!myGameIsOver.isEmpty() && opponentGameIsOver.isEmpty()){
            return gamePlayer.getGamePlayerId();
//            gameIsOver.put("looser", myHitAndSunk.get(myHitAndSunk.size() - 1).get("gamePlayerId"));
        }else{
            return null;
//            gameIsOver.put("looser", null);
        }


//        return gameIsOver.get("looser");
    }

    private Map<String, Integer> checkTurn(Set<GamePlayer> gamePlayerSet, GamePlayer currentGamePlayer){
        Map<String, Integer> lastTurnMap = new LinkedHashMap<>();
        gamePlayerSet.forEach(gamePlayer -> {
            if (gamePlayer.getGamePlayerId() != currentGamePlayer.getGamePlayerId()){
                lastTurnMap.put("opponentLastTurn", getLastSalvoTurn(gamePlayer.getSalvos()));

            }else{
                lastTurnMap.put("myLastTurn", getLastSalvoTurn(currentGamePlayer.getSalvos()));

            }
        });
        return lastTurnMap;
    }

    private Integer getLastSalvoTurn(Set<Salvo> salvoSet){
        Integer lastSalvoTurn = 0;
        List<Salvo> salvoList = salvoSet.stream().collect(toList());
        Comparator<Salvo> comparator = new Comparator<Salvo>() {
            @Override
            public int compare(Salvo o1, Salvo o2) {
                return o1.getTurnNumber().compareTo(o2.getTurnNumber());
            }
        };
        Collections.sort(salvoList, comparator);
        if(salvoList.size() != 0){
            lastSalvoTurn = salvoList.get(salvoList.size() - 1).getTurnNumber();
        }else{
            lastSalvoTurn = 0;
        }
        return lastSalvoTurn;
    }

    private Map<String, Object> getHitAndSunk(GamePlayer gamePlayer) {
        Map<String, Object> hitAndSunkMap = new LinkedHashMap<>();
        GamePlayer opponentGamePlayer = findOpponentGamePlayer(gamePlayer);
//        System.out.println("this is me" + gamePlayer.getPlayer().getName());
//        System.out.println("this is my opponent" + opponentGamePlayer.getPlayer().getName());
        if(opponentGamePlayer != null){
            Set<Salvo> gamePlayerSalvos = gamePlayer.getSalvos();
            Set<Ship> gamePlayerShips = gamePlayer.getShipTypes();
            Set<Ship> opponentShips = opponentGamePlayer.getShipTypes();
            Set<Salvo> opponentSalvos = opponentGamePlayer.getSalvos();
            List<Map<String, Object>> opponentHitAndSunk = getOneSalvo(gamePlayer, gamePlayerSalvos, opponentShips);
            List<Map<String, Object>> myHitAndSunk = getOneSalvo(opponentGamePlayer, opponentSalvos, gamePlayerShips);
            hitAndSunkMap.put("opponentHitAndSunk", opponentHitAndSunk);
            hitAndSunkMap.put("myHitAndSunk", myHitAndSunk);
        }
        return hitAndSunkMap;
    }

    private List<Map<String, Object>> getOneSalvo(GamePlayer gamePlayer, Set<Salvo> salvos, Set<Ship> ships){
        List<Salvo> salvoList = salvos.stream().collect(toList());
        Comparator<Salvo> comparator = new Comparator<Salvo>() {
            @Override
            public int compare(Salvo o1, Salvo o2) {
                return o1.getTurnNumber().compareTo(o2.getTurnNumber());
            }
        };
        Collections.sort(salvoList, comparator);
        return salvoList.stream().map(salvo -> {
            Map<String, Object> turnMap = new LinkedHashMap<>();
            turnMap.put("gamePlayerId", findOpponentGamePlayer(gamePlayer).getGamePlayerId());
            turnMap.put("turnNumber", salvo.getTurnNumber());
            turnMap.put("hits", getHit(salvo, ships));
            turnMap.put("totalDamage", getTotalDamage((List<Map>) turnMap.get("hits"), ships));
            turnMap.put("gameIsOver", getGameIsOver((Map) turnMap.get("totalDamage"), ships));
            return turnMap;
        }).collect(Collectors.toList());
    }

    private boolean getGameIsOver(Map totalDamageMap, Set<Ship> ships){
        List<Ship> shipList = ships.stream().collect(toList());
        Map<String, Boolean> checkIfGameIsOverMap = new HashMap<>();
        if(!totalDamageMap.isEmpty()){
            List<Ship> newShipList = shipList
                    .stream()
                    .filter(ship -> ship.getShipLocations().size() == (Integer) totalDamageMap.get(ship.getShipType()))
                    .collect(toList());
//            System.out.println(newShipList);
            if(newShipList.size() == 5){
                checkIfGameIsOverMap.put("gameIsOver", true);
            }else{
                checkIfGameIsOverMap.put("gameIsOver", false);
            }
        }else{
            checkIfGameIsOverMap.put("gameIsOver", false);
        }
        return checkIfGameIsOverMap.get("gameIsOver");
    }

    private Map<String, Object> getTotalDamage(List<Map> hitList, Set<Ship> ships){
        Map<String, Object> totalDamageMap = new LinkedHashMap<>();
            ships.forEach(ship ->
                    hitList.forEach(hit -> {
                            if(ship.getShipType() == hit.get("shipType")){
//                                System.out.println(ship.getDamage());
                                ship.setDamage(ship.getDamage() + (Integer) hit.get("turnShipDamage"));
                            }
                        totalDamageMap.put(ship.getShipType(), ship.getDamage());
                    }));
        return totalDamageMap;
    }

    private List<Map> getHit(Salvo gamePlayerSalvo, Set<Ship> opponentShips){
//        System.out.println(opponentShips);

        List<Map> hitList = new ArrayList<>();

        opponentShips.forEach(ship -> {

            List<String> hitLocation = getOneSalvoLocation(gamePlayerSalvo.getSalvoLocations(), ship);
            if(!hitLocation.isEmpty() && hitLocation != null){
//                Map<String, Integer> hitMap2 = new HashMap<>();
//                hitMap2.put(ship.getShipType(), getDamage(hitLocation));
//                ship.setDamage(ship.getDamage() + hitLocation.size());
//                if(ship.getDamage() == ship.getShipLength()){
//                    ship.setSunk(true);
//                }
                Map<String, Object> hitMap = new LinkedHashMap<>();
                hitMap.put("shipType", ship.getShipType());
                hitMap.put("hitsLocation", hitLocation);
                hitMap.put("turnShipDamage", hitLocation.size());
//                hitMap.put("totalDamage", ship.getDamage());
                hitMap.put("shipLength", ship.getShipLength());
//                hitMap.put("isSunk", ship.isSunk());

                hitList.add(hitMap);
//                hitList.add(hitMap2);
            }

//            System.out.println(hitMap);
//            System.out.println(hitList);
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
//        System.out.println(hitLocation);
        return hitLocation;
    }

    private GamePlayer findOpponentGamePlayer(GamePlayer gamePlayer){
        Map<String, GamePlayer> helperMap = new HashMap<>();
        if(gamePlayer.getGame().getGamePlayers().size() == 2){
            gamePlayer.getGame().getGamePlayers()
                    .stream()
                    .forEach(gp -> {
                        if(gp.getGamePlayerId() != gamePlayer.getGamePlayerId()){
                            helperMap.put("opponent", gp);
                        }
                    });
        }else{
            helperMap.put("opponent", null);
        }

        return helperMap.get("opponent");
    }

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