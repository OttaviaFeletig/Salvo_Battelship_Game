
package com.codeoftheweb.salvo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private ScoreRepository scoreRepository;
    private boolean methodCall = false;
    private int totalDamageDebug = 0;

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
        oneGameMap.put("winner", getWinnerOrTie(gamePlayer));
        return oneGameMap;
    }

    private Object getWinnerOrTie(GamePlayer gamePlayer){
        GamePlayer opponentGamePlayer = findOpponentGamePlayer(gamePlayer);
        Map<String, Integer> checkTurnMap = checkTurn(gamePlayer.getGame().getGamePlayers(), gamePlayer);
        if(checkTurnMap.get("opponentLastTurn") != checkTurnMap.get("myLastTurn")){
            methodCall = false;
            return null;
        }
        Date finishDate = new Date();
        Score gameScore = new Score();
        if(gameIsOver(gamePlayer.getShipTypes()) && gameIsOver(opponentGamePlayer.getShipTypes())){
            Score tieScore = new Score(finishDate, 0.5);
            gamePlayer.getGame().addScore(tieScore);
            gamePlayer.getPlayer().addScore(tieScore);
            scoreRepository.save(tieScore);
            System.out.println("it's a tie");
            methodCall = true;
            return "tie";
        }else if(gameIsOver(opponentGamePlayer.getShipTypes())){
            if(!checkIfScoreIsAlreadyAdded(gamePlayer)){
                addWinnerScore(gameScore, finishDate, gamePlayer);
            }
            if(!checkIfScoreIsAlreadyAdded(opponentGamePlayer)){
                addLooserScore(gameScore, finishDate, opponentGamePlayer);
            }
            System.out.println("game is over");
            methodCall = true;
            return gamePlayer.getGamePlayerId();
        }else if(gameIsOver(gamePlayer.getShipTypes())){
            if(!checkIfScoreIsAlreadyAdded(opponentGamePlayer)){
                addWinnerScore(gameScore, finishDate, opponentGamePlayer);
            }
            if(!checkIfScoreIsAlreadyAdded(gamePlayer)){
                System.out.println("score looser added");
                addLooserScore(gameScore, finishDate, gamePlayer);
            }
            System.out.println("game is over");
            methodCall = true;
            return opponentGamePlayer.getGamePlayerId();
        }else{
            System.out.println("game is not over");
            methodCall = false;
            return null;
        }
    }

    private void addWinnerScore(Score gameScore, Date finishDate, GamePlayer gamePlayer){
        gameScore.setScore(1.0);
        gameScore.setFinishDate(finishDate);
        gamePlayer.getPlayer().addScore(gameScore);
        gamePlayer.getGame().addScore(gameScore);
        scoreRepository.save(gameScore);
    }

    private void addLooserScore(Score gameScore, Date finishDate, GamePlayer gamePlayer){
        gameScore.setScore(0.0);
        gameScore.setFinishDate(finishDate);
        gamePlayer.getPlayer().addScore(gameScore);
        gamePlayer.getGame().addScore(gameScore);
        scoreRepository.save(gameScore);
    }

    private boolean checkIfScoreIsAlreadyAdded(GamePlayer gamePlayer){
        if(gamePlayer.getGame().getScores().size() == 0){
            System.out.println("score has not been added yet to the game");
            return false;
        }
        List<Score> gameScore = gamePlayer.getGame().getScores()
                .stream()
                .filter(score -> score.getPlayer() == gamePlayer.getPlayer())
                .collect(toList());
        System.out.println(gamePlayer.getPlayer().getName());
        System.out.println(gameScore);
        if(gameScore.size() > 0){
            System.out.println("score has been added already to the game player");
            return true;
        }else{
            System.out.println("score has not been added yet to the game player");
            return false;
        }
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

        List<Map<String, Object>> turnList = new ArrayList<>();

        if(salvoList.isEmpty()){
            return turnList;
        }
        salvoList.forEach(salvo -> {
            Map<String, Object> turnMap = new LinkedHashMap<>();
            turnMap.put("gamePlayerId", findOpponentGamePlayer(gamePlayer).getGamePlayerId());
            turnMap.put("turnNumber", salvo.getTurnNumber());
            turnMap.put("hits", getHit(salvo, ships));
//            turnMap.put("totalDamage", ships.stream().map(ship -> ship.getDamage()));
            turnMap.put("totalDamage",getTotalDamage(ships));
            turnMap.put("gameIsOver", gameIsOver(ships));

            turnList.add(turnMap);
        });
        return turnList;
    }

    private Map<String, Object> getTotalDamage(Set<Ship> ships){
        Map<String, Object> totalDamageMap = new LinkedHashMap<>();
        ships.forEach(ship -> {
            totalDamageMap.put(ship.getShipType(), ship.getHits().size());
        });
        return totalDamageMap;
    }

    private boolean gameIsOver(Set<Ship> ships){
        List<Ship> shipList = ships.stream().collect(toList());
        List<Ship> newShipList = new ArrayList<>();
        shipList.forEach(ship -> {
            if(ship.getHits().size() == ship.getShipLocations().size()){
                newShipList.add(ship);
            }

        });
        if(newShipList.size() == 5){
            return true;
        }else{
            return false;
        }
    }

    private List<Map> getHit(Salvo gamePlayerSalvo, Set<Ship> opponentShips){
        List<Map> hitList = new ArrayList<>();
        opponentShips.forEach(ship -> {
            getOneSalvoLocation(gamePlayerSalvo.getSalvoLocations(), ship);
            if(!ship.getHits().isEmpty()){
                Map<String, Object> hitMap = new LinkedHashMap<>();
                hitMap.put("shipType", ship.getShipType());
                hitMap.put("hitsLocation", ship.getHits());
                hitMap.put("turnShipDamage", ship.getHits().size());
                hitMap.put("shipLength", ship.getShipLocations().size());
                hitList.add(hitMap);
            }
        });
        return hitList;
    }

    private void getOneSalvoLocation(List<String> gamePlayerSalvoLocations, Ship opponentShip){
        for (int i = 0; i < gamePlayerSalvoLocations.size(); i++){
            if(opponentShip.getShipLocations().contains(gamePlayerSalvoLocations.get(i))){
                opponentShip.getHits().add(gamePlayerSalvoLocations.get(i).toString());
            }
        }
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
    @Bean
    public PasswordEncoder passwordEncoder2(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
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
        Player newPlayer = playerRepository.save(new Player(email, passwordEncoder2().encode(password), name));
        return new ResponseEntity<>(makeMapForResponseEntity("email", newPlayer.getEmail()), HttpStatus.CREATED);
    }

    private HashMap<String, Object> makeMapForResponseEntity(String key, Object value) {
        HashMap<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

}