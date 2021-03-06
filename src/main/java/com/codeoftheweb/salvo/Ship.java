package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long shipId;
    private String shipType;

    @ElementCollection
    @Column(name = "ship_location")
    private List<String> shipLocations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    private Integer damage = 0;
//    private Integer shipLength = 0;
    @ElementCollection
    @Column(name = "ship_hit")
    private Set<String> hits = new HashSet<>();
//    private Set<String> hits;
//    private boolean isSunk;

    public Ship() {}

    public Ship(String shipType, List<String> shipLocations){
        this.shipType = shipType;
        this.shipLocations = shipLocations;
//        this.shipLength = this.shipLocations.size();

//        this.isSunk = false;
    }

    public long getShipId() {
        return shipId;
    }

    public void setShipId(long shipId) {
        this.shipId = shipId;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public List<String> getShipLocations() {
        return shipLocations;
    }

    public void setShipLocations(List<String> shipLocations) {
        this.shipLocations = shipLocations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Integer getDamage() {
        return damage;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

//    public Integer getShipLength() {
//        return shipLength;
//    }
//
//    public void setShipLength(Integer shipLength) {
//        this.shipLength = shipLength;
//    }

//    public boolean isSunk() {
//        return isSunk;
//    }
//
//    public void setSunk(boolean sunk) {
//        isSunk = sunk;
//    }


    public Set<String> getHits() {
        return hits;
    }

    public void setHits(Set<String> hits) {
        this.hits = hits;
    }
}
