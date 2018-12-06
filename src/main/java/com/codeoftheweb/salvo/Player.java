package com.codeoftheweb.salvo;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long playerId;
    private String userName;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private List<GamePlayer> gamePlayers = new ArrayList<>();

//    private List<Game> getGames = new ArrayList<>();
    public Player() { }

    public Player(String email){
        this.userName = email;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long id) {
        this.playerId = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String email) {
        this.userName = email;
    }

    public void addGamePlayer(GamePlayer gamePlayer){
        gamePlayer.setPlayer(this);
        gamePlayers.add(gamePlayer);
    }

    public List<Game> getGames(){
        return gamePlayers.stream().map(sub -> sub.getGame()).collect(toList());
    }
}
