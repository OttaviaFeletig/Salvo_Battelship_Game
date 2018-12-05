package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long gameId;
    private Date date;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayers = new HashSet<>();
    public Game() { }

    public Game(Date date) {
        this.date = date;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long id) {
        this.gameId = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void addGamePlayer(GamePlayer gamePlayer){
        gamePlayer.setGame(this);
        gamePlayers.add(gamePlayer);
    }
    @JsonIgnore
    public List<Player> getPlayers(){

        return gamePlayers.stream().map(sub -> sub.getPlayer()).collect(toList());
    }

//    public Set<GamePlayer> getGamePlayers() {
//        return gamePlayers;
//    }
}
