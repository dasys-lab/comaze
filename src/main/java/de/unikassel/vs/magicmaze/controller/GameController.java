package de.unikassel.vs.magicmaze.controller;

import de.unikassel.vs.magicmaze.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
public class GameController {
  Map<UUID, Game> games = new HashMap<>();

  @GetMapping("/games")
  public Collection<Game> getGames() {
    return games.values();
  }

  @GetMapping("/game/{gameId}")
  public ResponseEntity<Game> getGame(
      @PathVariable("gameId") UUID gameId
  ) {
    if (games.containsKey(gameId)) {
      return ResponseEntity.ok(games.get(gameId));
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/game/create")
  public Game createGame(
      @RequestParam(value = "name", required = false) String name
  ) {
    Game game = new Game(name, GameConfigCreator.createLevel3());
    games.put(game.getUuid(), game);
    return game;
  }

  @PostMapping("/game/{gameId}/attend")
  public Player attendGame(
      @PathVariable("gameId") UUID gameId,
      @RequestParam(value = "playerName") String playerName,
      @RequestParam(value = "actions", required = false, defaultValue = "2") int actions
  ) {
    Game game = games.get(gameId);

    if (game == null) {
      throw new IllegalArgumentException("The game does not exist");
    }

    if (game.getFreeActions() < actions) {
      throw new IllegalArgumentException("The game is full or there are not enough actions left to assign");
    }

    Player player = new Player(playerName);
    game.getPlayers().add(player);
    for (int i = 0; i < actions; i++) {
      player.getActions().add(game.getUnassignedAction());
    }
    return player;
  }

  @PostMapping("/game/{gameId}/move")
  public ResponseEntity<String> move(
      @PathVariable("gameId") UUID gameId,
      @RequestParam("playerId") UUID playerId,
      @RequestParam("direction") Direction direction
  ) {
    Game game = games.get(gameId);

    if (game == null) {
      return ResponseEntity.badRequest().body("The game does not exist");
    }

    Optional<Player> optPlayer = game.getPlayers().stream().filter(player -> player.getUuid().equals(playerId)).findFirst();
    if (optPlayer.isEmpty()) {
      return ResponseEntity.badRequest().body("Player does not exist");
    }
    Player player = optPlayer.get();

    if (!game.getCurrentPlayer().equals(player)) {
      return ResponseEntity.badRequest().body("It is not your turn");
    }

    if (direction != null) { // skip move
      if (!player.getActions().contains(direction)) {
        return ResponseEntity.badRequest().body("You may not move in that direction");
      }

      Int2D newPosition = game.getAgentPosition().plus(direction.getDir());
      if (!newPosition.fitsIn(game.getConfig().getArenaSize())) {
        return ResponseEntity.badRequest().body("You may not move outside the arena");
      }

      if (game.getConfig().hasWallBetween(game.getAgentPosition(), newPosition)) {
        return ResponseEntity.badRequest().body("You may not move through walls");
      }

      if (!game.getMayStillMove()) {
        return ResponseEntity.badRequest().body("Game over: You have no moves left");
      }

      game.setAgentPosition(newPosition);
      game.increaseUsedMoves();

      Optional<Goal> reachedGoal = game.getConfig().getGoals().stream()
          .filter(goal -> goal.getPosition().equals(newPosition))
          .findAny();
      if (reachedGoal.isPresent()) {
        List<Color> reachedGoals = game.getReachedGoals();
        Color reachedGoalColor = reachedGoal.get().getColor();
        if (!reachedGoals.contains(reachedGoalColor)) {
          reachedGoals.add(reachedGoalColor);
        }
      }
    }
    player.setLastAction(direction);
    game.setNextPlayer();

    return ResponseEntity.ok().body("move ok");
  }

  @PostMapping("/game/{gameId}/skip")
  public ResponseEntity<String> skip(
      @PathVariable("gameId") UUID gameId,
      @RequestParam("playerId") UUID playerId
  ) {
    return move(gameId, playerId, null);
  }
}
