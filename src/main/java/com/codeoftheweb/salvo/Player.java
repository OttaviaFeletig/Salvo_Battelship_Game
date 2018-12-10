package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long playerId;
    private String userName;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

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

    public Set<Game> getGames(){
        return gamePlayers.stream().map(sub -> sub.getGame()).collect(Collectors.toSet());
    }
}
