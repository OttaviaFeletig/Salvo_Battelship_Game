package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
			Player username1 = new Player("j.bauer@ctu.gov", "24", "Jack Bauer");
		    Player username2 = new Player("c.obrian@ctu.gov", "42", "Chloe O'Brian");
		    Player username3 = new Player("kim_bauer@gmail.com", "kb", "Kim Bauer");
		    Player username4 = new Player("t.almeida@ctu.gov", "mole", "Tony Almeida");

			Date date = new Date();
			Date date1 = Date.from(date.toInstant().plusSeconds(3600));
			Date date2 = Date.from(date.toInstant().plusSeconds(7200));
			Game game1 = new Game(date);
			Game game2 = new Game(date1);
			Game game3 = new Game(date2);
			Game game4 = new Game(date2);
			Game game5 = new Game(date2);

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

            GamePlayer gamePlayer7 = new GamePlayer(date2);
            GamePlayer gamePlayer8 = new GamePlayer(date2);
            username1.addGamePlayer(gamePlayer7);
            username4.addGamePlayer(gamePlayer8);
            game4.addGamePlayer(gamePlayer7);
            game4.addGamePlayer(gamePlayer8);

            GamePlayer gamePlayer9 = new GamePlayer(date2);
            GamePlayer gamePlayer10 = new GamePlayer(date2);
            username1.addGamePlayer(gamePlayer9);
            username4.addGamePlayer(gamePlayer10);
            game5.addGamePlayer(gamePlayer9);
            game5.addGamePlayer(gamePlayer10);

			List<String> shipLocation1 = Arrays.asList("H2", "H3", "H4");
			List<String> shipLocation2 = Arrays.asList("E1", "F1", "G1");
			List<String> shipLocation3 = Arrays.asList("B4", "B5");
			List<String> shipLocation4 = Arrays.asList("B5", "C5", "D5");
			List<String> shipLocation5 = Arrays.asList("F1", "F2");
			List<String> shipLocation6 = Arrays.asList("C6", "C7");
			List<String> shipLocation7 = Arrays.asList("A2", "A3", "A4");
			List<String> shipLocation8 = Arrays.asList("G6", "H6");

			//game 4 - game player 7
			List<String> shipLocation9 = Arrays.asList("A1", "A2", "A3", "A4", "A5");
			List<String> shipLocation10 = Arrays.asList("B1", "B2", "B3", "B4");
			List<String> shipLocation11 = Arrays.asList("C1", "C2", "C3");
			List<String> shipLocation12 = Arrays.asList("D1", "D2", "D3");
			List<String> shipLocation13 = Arrays.asList("E1", "E2");

			//game 4 - game player 8

			List<String> shipLocation14 = Arrays.asList("F1", "F2", "F3", "F4", "F5");
			List<String> shipLocation15 = Arrays.asList("G1", "G2", "G3", "G4");
			List<String> shipLocation16 = Arrays.asList("H1", "H2", "H3");
			List<String> shipLocation17 = Arrays.asList("I1", "I2", "I3");
			List<String> shipLocation18 = Arrays.asList("J1", "J2");

			//game 5 - game player 9

            List<String> shipLocation19 = Arrays.asList("A1", "A2", "A3", "A4", "A5");
            List<String> shipLocation20 = Arrays.asList("B1", "B2", "B3", "B4");
            List<String> shipLocation21 = Arrays.asList("C1", "C2", "C3");
            List<String> shipLocation22 = Arrays.asList("D1", "D2", "D3");
            List<String> shipLocation23 = Arrays.asList("E1", "E2");

            //game 5 - game player 10

            List<String> shipLocation24 = Arrays.asList("F1", "F2", "F3", "F4", "F5");
            List<String> shipLocation25 = Arrays.asList("G1", "G2", "G3", "G4");
            List<String> shipLocation26 = Arrays.asList("H1", "H2", "H3");
            List<String> shipLocation27 = Arrays.asList("I1", "I2", "I3");
            List<String> shipLocation28 = Arrays.asList("J1", "J2");

			Ship shipType1_1 = new Ship("submarine", shipLocation2);
			Ship shipType1_2 = new Ship("destroyer", shipLocation1);
			Ship shipType1_3 = new Ship("p_boat", shipLocation3);
			Ship shipType2_1 = new Ship("submarine", shipLocation4);
			Ship shipType2_2 = new Ship("p_boat", shipLocation5);
			Ship shipType3_1 = new Ship("destroyer", shipLocation4);
			Ship shipType3_2 = new Ship("p_boat", shipLocation6);
			Ship shipType4_1 = new Ship("submarine", shipLocation7);
			Ship shipType4_2 = new Ship("p_boat", shipLocation8);
			Ship shipType5_1 = new Ship("destroyer", shipLocation6);
			Ship shipType5_2 = new Ship("p_boat", shipLocation3);
			Ship shipType6_1 = new Ship("p_boat", shipLocation2);
			Ship shipType6_2 = new Ship("submarine", shipLocation7);

			//game 4 AND game 5
			Ship shipType7_1 = new Ship("aircraft_carrier", shipLocation9);
			Ship shipType7_2 = new Ship("battleship", shipLocation10);
			Ship shipType7_3 = new Ship("submarine", shipLocation11);
			Ship shipType7_4 = new Ship("destroyer", shipLocation12);
			Ship shipType7_5 = new Ship("p_boat", shipLocation13);
			Ship shipType8_1 = new Ship("aircraft_carrier", shipLocation14);
			Ship shipType8_2 = new Ship("battleship", shipLocation15);
			Ship shipType8_3 = new Ship("submarine", shipLocation16);
			Ship shipType8_4 = new Ship("destroyer", shipLocation17);
			Ship shipType8_5 = new Ship("p_boat", shipLocation18);
            Ship shipType9_1 = new Ship("aircraft_carrier", shipLocation19);
            Ship shipType9_2 = new Ship("battleship", shipLocation20);
            Ship shipType9_3 = new Ship("submarine", shipLocation21);
            Ship shipType9_4 = new Ship("destroyer", shipLocation22);
            Ship shipType9_5 = new Ship("p_boat", shipLocation23);
            Ship shipType10_1 = new Ship("aircraft_carrier", shipLocation24);
            Ship shipType10_2 = new Ship("battleship", shipLocation25);
            Ship shipType10_3 = new Ship("submarine", shipLocation26);
            Ship shipType10_4 = new Ship("destroyer", shipLocation27);
            Ship shipType10_5 = new Ship("p_boat", shipLocation28);

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

			//game 4 - game player 7
			List<String> salvoLocation13 = Arrays.asList("A1", "A2", "A3", "A4", "A5");
			List<String> salvoLocation14 = Arrays.asList("B1", "B2", "B3", "B4", "C1");
			List<String> salvoLocation15 = Arrays.asList("C2", "C3", "D1", "D2", "D3");


			//game 4 - game player 8

			List<String> salvoLocation16 = Arrays.asList("A1", "A2", "A3", "A4", "A5");
			List<String> salvoLocation17 = Arrays.asList("B1", "B2", "B3", "B4", "C1");
			List<String> salvoLocation18 = Arrays.asList("C2", "C3", "D1", "D2", "D3");

			// game 5 - game player 10

            List<String> salvoLocation19 = Arrays.asList("F1", "F2", "F3", "F4", "F5");
            List<String> salvoLocation20 = Arrays.asList("G1", "G2", "G3", "G4", "H1");
            List<String> salvoLocation21 = Arrays.asList("H2", "H3", "I1", "I2", "I3");

            //game 5 - game player 9

            List<String> salvoLocation22 = Arrays.asList("A1", "A2", "A3", "A4", "A5");
            List<String> salvoLocation23 = Arrays.asList("B1", "B2", "B3", "B4", "C1");
            List<String> salvoLocation24 = Arrays.asList("C2", "C3", "D1", "D2", "D3");

			Integer turn1 = 1;
			Integer turn2 = 2;
			Integer turn3 = 3;

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

			//game 4

			Salvo salvo4_1_1 = new Salvo(turn1, salvoLocation13);
			Salvo salvo4_1_2 = new Salvo(turn2, salvoLocation14);
			Salvo salvo4_1_3 = new Salvo(turn3, salvoLocation15);
			Salvo salvo4_2_1 = new Salvo(turn1, salvoLocation16);
			Salvo salvo4_2_2 = new Salvo(turn2, salvoLocation17);
			Salvo salvo4_2_3 = new Salvo(turn3, salvoLocation18);

			// game 5

            Salvo salvo5_1_1 = new Salvo(turn1, salvoLocation22);
            Salvo salvo5_1_2 = new Salvo(turn2, salvoLocation23);
            Salvo salvo5_1_3 = new Salvo(turn3, salvoLocation24);
            Salvo salvo5_2_1 = new Salvo(turn1, salvoLocation19);
            Salvo salvo5_2_2 = new Salvo(turn2, salvoLocation20);
            Salvo salvo5_2_3 = new Salvo(turn3, salvoLocation21);

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

			//game 4 one winner

			gamePlayer7.addShipTypes(shipType7_1);
			gamePlayer7.addShipTypes(shipType7_2);
			gamePlayer7.addShipTypes(shipType7_3);
			gamePlayer7.addShipTypes(shipType7_4);
			gamePlayer7.addShipTypes(shipType7_5);
			gamePlayer7.addSalvos(salvo4_1_1);
			gamePlayer7.addSalvos(salvo4_1_2);
			gamePlayer7.addSalvos(salvo4_1_3);
			gamePlayer8.addShipTypes(shipType8_1);
			gamePlayer8.addShipTypes(shipType8_2);
			gamePlayer8.addShipTypes(shipType8_3);
			gamePlayer8.addShipTypes(shipType8_4);
			gamePlayer8.addShipTypes(shipType8_5);
			gamePlayer8.addSalvos(salvo4_2_1);
			gamePlayer8.addSalvos(salvo4_2_2);
			gamePlayer8.addSalvos(salvo4_2_3);


			//game 5 tie

            gamePlayer9.addShipTypes(shipType9_1);
            gamePlayer9.addShipTypes(shipType9_2);
            gamePlayer9.addShipTypes(shipType9_3);
            gamePlayer9.addShipTypes(shipType9_4);
            gamePlayer9.addShipTypes(shipType9_5);
            gamePlayer9.addSalvos(salvo5_2_1);
            gamePlayer9.addSalvos(salvo5_2_2);
            gamePlayer9.addSalvos(salvo5_2_3);
            gamePlayer10.addShipTypes(shipType10_1);
            gamePlayer10.addShipTypes(shipType10_2);
            gamePlayer10.addShipTypes(shipType10_3);
            gamePlayer10.addShipTypes(shipType10_4);
            gamePlayer10.addShipTypes(shipType10_5);
            gamePlayer10.addSalvos(salvo5_1_1);
            gamePlayer10.addSalvos(salvo5_1_2);
            gamePlayer10.addSalvos(salvo5_1_3);

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
            repositoryGame.save(game4);
            repositoryGame.save(game5);

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
			repositoryGamePlayer.save(gamePlayer7);
			repositoryGamePlayer.save(gamePlayer8);
			repositoryGamePlayer.save(gamePlayer9);
			repositoryGamePlayer.save(gamePlayer10);

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
			repositoryShip.save(shipType7_1);
			repositoryShip.save(shipType7_2);
			repositoryShip.save(shipType7_3);
			repositoryShip.save(shipType7_4);
			repositoryShip.save(shipType7_5);
			repositoryShip.save(shipType8_1);
			repositoryShip.save(shipType8_2);
			repositoryShip.save(shipType8_3);
			repositoryShip.save(shipType8_4);
			repositoryShip.save(shipType8_5);
			repositoryShip.save(shipType9_1);
			repositoryShip.save(shipType9_2);
			repositoryShip.save(shipType9_3);
			repositoryShip.save(shipType9_4);
			repositoryShip.save(shipType9_5);
			repositoryShip.save(shipType10_1);
			repositoryShip.save(shipType10_2);
			repositoryShip.save(shipType10_3);
			repositoryShip.save(shipType10_4);
			repositoryShip.save(shipType10_5);

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
			repositorySalvo.save(salvo4_1_1);
			repositorySalvo.save(salvo4_1_2);
			repositorySalvo.save(salvo4_1_3);
			repositorySalvo.save(salvo4_2_1);
			repositorySalvo.save(salvo4_2_2);
			repositorySalvo.save(salvo4_2_3);
			repositorySalvo.save(salvo5_1_1);
			repositorySalvo.save(salvo5_1_2);
			repositorySalvo.save(salvo5_1_3);
			repositorySalvo.save(salvo5_2_1);
			repositorySalvo.save(salvo5_2_2);
			repositorySalvo.save(salvo5_2_3);

			repositoryScore.save(score1);
			repositoryScore.save(score2);
			repositoryScore.save(score3);
			repositoryScore.save(score4);

        });
	}

	@Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;


	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(email-> {
			Player player = playerRepository.findByEmail(email);
			if (player != null) {
				return new User(player.getEmail(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + email);
			}
		});
	}
}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter{
    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http.authorizeRequests()
                .antMatchers("/api/games").permitAll()
                .antMatchers("/api/leader_board").permitAll()
                .antMatchers("/web/games.html").permitAll()
                .antMatchers("/web/index.html").permitAll()
                .antMatchers("/web/style/style.css").permitAll()
                .antMatchers("/web/script/games.js").permitAll()
				.antMatchers("/api/players").permitAll()
                .antMatchers("/api/game_view/*").hasAnyAuthority("USER")
                .antMatchers("/rest/*").permitAll()
                .anyRequest().fullyAuthenticated()
                .and()
            .formLogin()
                .usernameParameter("email")
                .passwordParameter("password")
                .loginPage("/api/login");
        http.logout().logoutUrl("/api/logout");

        http.csrf().disable();
        http.exceptionHandling().authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED));
        http.formLogin().successHandler((request, response, authentication) -> clearAuthenticationAttribute(request));
        http.formLogin().failureHandler((request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED));
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttribute(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session != null){
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}

