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
	public CommandLineRunner initData(PlayerRepository repositoryPlayer, GameRepository repositoryGame, GamePlayerRepository repositoryGamePlayer, ShipRepository repositoryShip, SalvoRepository repositorySalvo, ScoreRepository repositoryScore) {
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

			List<String> shipLocation1 = Arrays.asList("H2", "H3", "H4");
			List<String> shipLocation2 = Arrays.asList("E1", "F1", "G1");
			List<String> shipLocation3 = Arrays.asList("B4", "B5");
			List<String> shipLocation4 = Arrays.asList("B5", "C5", "D5");
			List<String> shipLocation5 = Arrays.asList("F1", "F2");
			List<String> shipLocation6 = Arrays.asList("C6", "C7");
			List<String> shipLocation7 = Arrays.asList("A2", "A3", "A4");
			List<String> shipLocation8 = Arrays.asList("G6", "H6");

			Ship shipType1_1 = new Ship("Submarine", shipLocation2);
			Ship shipType1_2 = new Ship("Destroyer", shipLocation1);
			Ship shipType1_3 = new Ship("Patrol Boat", shipLocation3);
			Ship shipType2_1 = new Ship("Submarine", shipLocation4);
			Ship shipType2_2 = new Ship("Patrol Boat", shipLocation5);
			Ship shipType3_1 = new Ship("Destroyer", shipLocation4);
			Ship shipType3_2 = new Ship("Patrol Boat", shipLocation6);
			Ship shipType4_1 = new Ship("Submarine", shipLocation7);
			Ship shipType4_2 = new Ship("Patrol Boat", shipLocation8);
			Ship shipType5_1 = new Ship("Destroyer", shipLocation6);
			Ship shipType5_2 = new Ship("Patrol Boat", shipLocation3);
			Ship shipType6_1 = new Ship("Patrol Boat", shipLocation2);
			Ship shipType6_2 = new Ship("Submarine", shipLocation7);

			List<String> salvoLocation1 = Arrays.asList("B5", "C5", "F1");
			List<String> salvoLocation2 = Arrays.asList("F2", "D5", "H2");
			List<String> salvoLocation3 = Arrays.asList("A2", "A4", "G6");
			List<String> salvoLocation4 = Arrays.asList("A3", "H6");
			List<String> salvoLocation5 = Arrays.asList("G6", "H6", "A4");
			List<String> salvoLocation6 = Arrays.asList("A2", "A3", "D8");
			List<String> salvoLocation7 = Arrays.asList("B4", "B5", "B6");
			List<String> salvoLocation8 = Arrays.asList("E1", "H3", "A2");
			List<String> salvoLocation9 = Arrays.asList("B5", "D5", "C7");
			List<String> salvoLocation10 = Arrays.asList("C5", "C6");
			List<String> salvoLocation11 = Arrays.asList("H1", "H2", "H3");
			List<String> salvoLocation12 = Arrays.asList("E1", "F2", "G3");

			Integer turn1 = 1;
			Integer turn2 = 2;

			Salvo salvo1_1_1 = new Salvo(turn1, salvoLocation1);
			Salvo salvo1_1_2 = new Salvo(turn2, salvoLocation2);
			Salvo salvo1_2_1 = new Salvo(turn1, salvoLocation7);
			Salvo salvo1_2_2 = new Salvo(turn2, salvoLocation8);
			Salvo salvo2_1_1 = new Salvo(turn1, salvoLocation3);
			Salvo salvo2_1_2 = new Salvo(turn2, salvoLocation4);
			Salvo salvo2_2_1 = new Salvo(turn1, salvoLocation9);
			Salvo salvo2_2_2 = new Salvo(turn2, salvoLocation10);
			Salvo salvo3_1_1 = new Salvo(turn1, salvoLocation5);
			Salvo salvo3_1_2 = new Salvo(turn2, salvoLocation6);
			Salvo salvo3_2_1 = new Salvo(turn1, salvoLocation11);
			Salvo salvo3_2_2 = new Salvo(turn2, salvoLocation12);

			//game 1
			gamePlayer1.addShipTypes(shipType1_1);
			gamePlayer1.addShipTypes(shipType1_2);
			gamePlayer1.addShipTypes(shipType1_3);
			gamePlayer1.addSalvos(salvo1_1_1);
			gamePlayer1.addSalvos(salvo1_1_2);
			gamePlayer2.addShipTypes(shipType2_1);
			gamePlayer2.addShipTypes(shipType2_2);
			gamePlayer2.addSalvos(salvo1_2_1);
			gamePlayer2.addSalvos(salvo1_2_2);

			//game 2

			gamePlayer3.addShipTypes(shipType3_1);
			gamePlayer3.addShipTypes(shipType3_2);
			gamePlayer3.addSalvos(salvo2_1_1);
			gamePlayer3.addSalvos(salvo2_1_2);
			gamePlayer4.addShipTypes(shipType4_1);
			gamePlayer4.addShipTypes(shipType4_2);
			gamePlayer4.addSalvos(salvo2_2_1);
			gamePlayer4.addSalvos(salvo2_2_2);

			//game 3

			gamePlayer5.addShipTypes(shipType5_1);
			gamePlayer5.addShipTypes(shipType5_2);
			gamePlayer5.addSalvos(salvo3_1_1);
			gamePlayer5.addSalvos(salvo3_1_2);
			gamePlayer6.addShipTypes(shipType6_1);
			gamePlayer6.addShipTypes(shipType6_2);
			gamePlayer6.addSalvos(salvo3_2_1);
			gamePlayer6.addSalvos(salvo3_2_2);

			Date finishDate = Date.from(date.toInstant().plusSeconds(1800));
			Date finishDate1 = Date.from(date1.toInstant().plusSeconds(1800));

			Score score1 = new Score(finishDate, 1.0);
			Score score2 = new Score(finishDate, 0.0);
			Score score3 = new Score(finishDate1, 0.5);
			Score score4 = new Score(finishDate1, 0.5);
//			Score score5 = new Score(finishDate, 2.0);
//			Score score6 = new Score(finishDate, 1.5);

			//game 1
			game1.addScore(score1);
			username1.addScore(score1);
			game1.addScore(score2);
			username2.addScore(score2);

			//game 2

            game2.addScore(score3);
            username1.addScore(score3);
            game2.addScore(score4);
            username2.addScore(score4);

            //game3

//            game3.addScore(score5);
//            username3.addScore(score5);
//            game3.addScore(score6);
//            username3.addScore(score6);

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

			repositorySalvo.save(salvo1_1_1);
			repositorySalvo.save(salvo1_1_2);
			repositorySalvo.save(salvo1_2_1);
			repositorySalvo.save(salvo1_2_2);
			repositorySalvo.save(salvo2_1_1);
			repositorySalvo.save(salvo2_1_2);
			repositorySalvo.save(salvo2_2_1);
			repositorySalvo.save(salvo2_2_2);
			repositorySalvo.save(salvo3_1_1);
			repositorySalvo.save(salvo3_1_2);
			repositorySalvo.save(salvo3_2_1);
			repositorySalvo.save(salvo3_2_2);

			repositoryScore.save(score1);
			repositoryScore.save(score2);
			repositoryScore.save(score3);
			repositoryScore.save(score4);

        });
	}
}
