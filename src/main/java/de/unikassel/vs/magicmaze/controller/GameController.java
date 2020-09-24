package de.unikassel.vs.magicmaze.controller;

import de.unikassel.vs.magicmaze.model.Game;
import de.unikassel.vs.magicmaze.model.GameConfigCreator;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class GameController {
  Map<UUID, Game> games = new HashMap<>();

  @GetMapping("/games")
  public Collection<Game> getGames() {
    return games.values();
  }

  @GetMapping("/game/{gameId}")
  public Game getGame(@PathVariable("gameId") UUID gameId) {
    return games.get(gameId);
  }

  @PostMapping("/game/create")
  public Game createGame(@RequestParam("name") String name) {
    Game game = new Game(name, GameConfigCreator.createLevel1());
    games.put(game.getUuid(), game);
    return game;
  }
}
