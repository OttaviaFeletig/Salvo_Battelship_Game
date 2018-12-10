package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository repositoryPlayer, GameRepository repositoryGame, GamePlayerRepository repositoryGamePlayer, ShipRepository repositoryShip) {
		return (args -> {
		    Player username1 = new Player("j.bauer@ctu.gov");
		    Player username2 = new Player("c.obrian@ctu.gov");
		    Player username3 = new Player("kim_bauer@gmail.com");
		    Player username4 = new Player("t.almeida@ctu.gov");

			Date date = new Date();
			Date date1 = Date.from(date.toInstant().plusSeconds(3600));
			Date date2 = Date.from(date.toInstant().plusSeconds(7200));
			Game game1 = new Game(date);
			Game game2 = new Game(date1);
			Game game3 = new Game(date2);

			GamePlayer gamePlayer1 = new GamePlayer(date);
			GamePlayer gamePlayer2 = new GamePlayer(date);

			username1.addGamePlayer(gamePlayer1);
			username2.addGamePlayer(gamePlayer2);
			game1.addGamePlayer(gamePlayer1);
			game1.addGamePlayer(gamePlayer2);

			GamePlayer gamePlayer3 = new GamePlayer(date1);
			GamePlayer gamePlayer4 = new GamePlayer(date1);
            username1.addGamePlayer(gamePlayer3);
			username2.addGamePlayer(gamePlayer4);
            game2.addGamePlayer(gamePlayer3);
			game2.addGamePlayer(gamePlayer4);

            GamePlayer gamePlayer5 = new GamePlayer(date2);
			GamePlayer gamePlayer6 = new GamePlayer(date2);
            username3.addGamePlayer(gamePlayer5);
            username4.addGamePlayer(gamePlayer6);
            game3.addGamePlayer(gamePlayer5);
            game3.addGamePlayer(gamePlayer6);

			List<String> location1 = Arrays.asList("H2", "H3", "H4");
			List<String> location2 = Arrays.asList("E1", "F1", "G1");
			List<String> location3 = Arrays.asList("B4", "B5");
			List<String> location4 = Arrays.asList("B5", "C5", "D5");
			List<String> location5 = Arrays.asList("F1", "F2");
			List<String> location6 = Arrays.asList("C6", "C7");
			List<String> location7 = Arrays.asList("A2", "A3", "A4");
			List<String> location8 = Arrays.asList("G6", "H6");

			Ship shipType1_1 = new Ship("Submarine", location2);
			Ship shipType1_2 = new Ship("Destroyer", location1);
			Ship shipType1_3 = new Ship("Patrol Boat", location3);
			Ship shipType2_1 = new Ship("Submarine", location4);
			Ship shipType2_2 = new Ship("Patrol Boat", location5);
			Ship shipType3_1 = new Ship("Destroyer", location4);
			Ship shipType3_2 = new Ship("Patrol Boat", location6);
			Ship shipType4_1 = new Ship("Submarine", location7);
			Ship shipType4_2 = new Ship("Patrol Boat", location8);
			Ship shipType5_1 = new Ship("Destroyer", location6);
			Ship shipType5_2 = new Ship("Patrol Boat", location3);
			Ship shipType6_1 = new Ship("Patrol Boat", location2);
			Ship shipType6_2 = new Ship("Submarine", location7);

			//game 1
			gamePlayer1.addShipTypes(shipType1_1);
			gamePlayer1.addShipTypes(shipType1_2);
			gamePlayer1.addShipTypes(shipType1_3);
			gamePlayer2.addShipTypes(shipType2_1);
			gamePlayer2.addShipTypes(shipType2_2);

			//game 2

			gamePlayer3.addShipTypes(shipType3_1);
			gamePlayer3.addShipTypes(shipType3_2);
			gamePlayer4.addShipTypes(shipType4_1);
			gamePlayer4.addShipTypes(shipType4_2);

			//game 3

			gamePlayer5.addShipTypes(shipType5_1);
			gamePlayer5.addShipTypes(shipType5_2);
			gamePlayer6.addShipTypes(shipType6_1);
			gamePlayer6.addShipTypes(shipType6_2);

			repositoryGame.save(game1);
			repositoryGame.save(game2);
			repositoryGame.save(game3);

			repositoryPlayer.save(username1);
			repositoryPlayer.save(username2);
			repositoryPlayer.save(username3);
			repositoryPlayer.save(username4);

			repositoryGamePlayer.save(gamePlayer1);
			repositoryGamePlayer.save(gamePlayer2);
			repositoryGamePlayer.save(gamePlayer3);
			repositoryGamePlayer.save(gamePlayer4);
			repositoryGamePlayer.save(gamePlayer5);
			repositoryGamePlayer.save(gamePlayer6);

			repositoryShip.save(shipType1_1);
			repositoryShip.save(shipType1_2);
			repositoryShip.save(shipType1_3);
			repositoryShip.save(shipType2_1);
			repositoryShip.save(shipType2_2);
			repositoryShip.save(shipType3_1);
			repositoryShip.save(shipType3_2);
			repositoryShip.save(shipType4_1);
			repositoryShip.save(shipType4_2);
			repositoryShip.save(shipType5_1);
			repositoryShip.save(shipType5_2);
			repositoryShip.save(shipType6_1);
			repositoryShip.save(shipType6_2);

        });
	}
}
