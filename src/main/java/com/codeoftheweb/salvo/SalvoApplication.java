package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;


@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository repositoryPlayer, GameRepository repositoryGame) {
		return (args -> {
			repositoryPlayer.save(new Player("j.bauer@ctu.gov"));
            repositoryPlayer.save(new Player("c.obrian@ctu.gov"));
            repositoryPlayer.save(new Player("kim_bauer@gmail.com"));
            repositoryPlayer.save(new Player("t.almeida@ctu.gov"));

			Date date = new Date();
			Date date1 = Date.from(date.toInstant().plusSeconds(3600));
			Date date2 = Date.from(date.toInstant().plusSeconds(7200));
			Date date3 = Date.from(date.toInstant().plusSeconds(10800));
			repositoryGame.save(new Game(date1));
			repositoryGame.save(new Game(date2));
			repositoryGame.save(new Game(date3));
        });
	}
}
