package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;


@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository repositoryPlayer, GameRepository repositoryGame, GamePlayerRepository repositoryGamePlayer) {
		return (args -> {
		    Player username1 = new Player("j.bauer@ctu.gov");
		    Player username2 = new Player("c.obrian@ctu.gov");
		    Player username3 = new Player("kim_bauer@gmail.com");
		    Player username4 = new Player("t.almeida@ctu.gov");

			repositoryPlayer.save(username1);
            repositoryPlayer.save(username2);
            repositoryPlayer.save(username3);
            repositoryPlayer.save(username4);

			Date date = new Date();
			Date date1 = Date.from(date.toInstant().plusSeconds(3600));
			Date date2 = Date.from(date.toInstant().plusSeconds(7200));
			Date date3 = Date.from(date.toInstant().plusSeconds(10800));
			Game game1 = new Game(date1);
			Game game2 = new Game(date2);
			Game game3 = new Game(date3);
			repositoryGame.save(game1);
			repositoryGame.save(game2);
			repositoryGame.save(game3);

			GamePlayer gamePlayer1 = new GamePlayer(date1);
			username1.addGamePlayer(gamePlayer1);
//			username2.addGamePlayer(gamePlayer1);
			game1.addGamePlayer(gamePlayer1);

			GamePlayer gamePlayer2 = new GamePlayer(date2);
//            username1.addGamePlayer(gamePlayer2);
			username2.addGamePlayer(gamePlayer2);
            game2.addGamePlayer(gamePlayer2);

            GamePlayer gamePlayer3 = new GamePlayer(date3);
            username3.addGamePlayer(gamePlayer3);
            game3.addGamePlayer(gamePlayer3);

			repositoryGamePlayer.save(gamePlayer1);
			repositoryGamePlayer.save(gamePlayer2);
			repositoryGamePlayer.save(gamePlayer3);
        });
	}
}
