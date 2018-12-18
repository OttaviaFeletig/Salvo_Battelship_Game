package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long gamePlayerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;
    private Date date;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Ship> shipTypes = new HashSet<>();
    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Salvo> salvos = new HashSet<>();


    public GamePlayer() { }
    public GamePlayer(Date date){
        this.date = date;
    }

    public long getGamePlayerId() {
        return gamePlayerId;
    }

    public void setGamePlayerId(long id) {
        this.gamePlayerId = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void addShipTypes(Ship shipType){
        shipType.setGamePlayer(this);
        shipTypes.add(shipType);
    }

    public Set<Ship> getShipTypes() {
        return shipTypes;
    }

    public void setShipTypes(Set<Ship> shipTypes) {
        this.shipTypes = shipTypes;
    }

    public void addSalvos(Salvo salvo){
        salvo.setGamePlayer(this);
        salvos.add(salvo);
    }

    public Set<Salvo> getSalvos() {
        return salvos;
    }

    public void setSalvos(Set<Salvo> salvos) {
        this.salvos = salvos;
    }

    public LinkedHashMap<String, Object> getScoreInGame(Game game){
        return player.getScores()
                .stream()
                .filter(score -> game.equals(score.getGame()))
                .map(score -> new LinkedHashMap<String, Object>(){{
                    put("id", score.getScoreId());
                    put("scorePoint", score.getScore());
                }})
                .findFirst()
                .orElse(null);
    }
}
