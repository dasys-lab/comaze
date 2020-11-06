package de.unikassel.vs.comaze.controller;

import de.unikassel.vs.comaze.model.*;
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
  public ResponseEntity<?> getGame(
      @PathVariable("gameId") UUID gameId
  ) {
    if (games.containsKey(gameId)) {
      return ResponseEntity.ok(games.get(gameId));
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/game/create")
  public ResponseEntity<?> createGame(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "level", required = false, defaultValue = "3") String level
  ) {
    GameConfig config = GameConfigCreator.createLevel(level);
    if (config == null) {
      return ResponseEntity.badRequest().body("There is no such level");
    }

    Game game = new Game(name, config);
    games.put(game.getUuid(), game);
    return ResponseEntity.ok(game);
  }

  @PostMapping("/game/{gameId}/attend")
  public ResponseEntity<?> attendGame(
      @PathVariable("gameId") UUID gameId,
      @RequestParam(value = "playerName") String playerName,
      @RequestParam(value = "actions", required = false, defaultValue = "2") int actions
  ) {
    Game game = games.get(gameId);

    if (game == null) {
      return ResponseEntity.badRequest().body("The game does not exist");
    }

    if (game.getUnassignedActions() < actions) {
      return ResponseEntity.badRequest().body("The game is full or there are not enough actions left to assign");
    }

    Player player = new Player(playerName);
    game.getPlayers().add(player);
    for (int i = 0; i < actions; i++) {
      player.getActions().add(game.getUnassignedAction());
    }

    if (game.getConfig().isHasSecretGoalRules()) {
      PlayerWithSecretGoalRule rulePlayer = new PlayerWithSecretGoalRule(player);
      SecretGoalRule rule = game.getUnassignedSecretGoalRule();
      rule.setPlayer(player);
      rulePlayer.setSecretGoalRule(rule);
      return ResponseEntity.ok(rulePlayer);
    } else {
      return ResponseEntity.ok(player);
    }
  }

  @PostMapping("/game/{gameId}/move")
  public ResponseEntity<?> move(
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

    if (!game.getState().getStarted()) {
      return ResponseEntity.badRequest().body("The game has not started yet because there are not enough players");
    }

    if (!game.getCurrentPlayer().equals(player)) {
      return ResponseEntity.badRequest().body("It is not your turn");
    }

    if (direction != null) { // direction == null => skipping move
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

      if (game.getState().getOver()) {
        return ResponseEntity.badRequest().body("Game over");
      }

      game.setAgentPosition(newPosition);
      game.increaseUsedMoves();

      // strike goal if reached
      game.getUnreachedGoals().stream()
          .filter(goal -> goal.getPosition().equals(newPosition))
          .findAny()
          .ifPresent(goal -> game.getUnreachedGoals().remove(goal));

      // use bonusTime if reached
      game.getUnusedBonusTimes().stream()
          .filter(bonusTime -> bonusTime.getPosition().equals(newPosition))
          .findAny()
          .ifPresent(bonusTime -> {
            game.getUnusedBonusTimes().remove(bonusTime);
            game.addBonusMoves(bonusTime.getAmount());
          });
    }
    player.setLastAction(direction);
    game.setNextPlayer();

    return ResponseEntity.ok().body(game);
  }

  @PostMapping("/game/{gameId}/skip")
  public ResponseEntity<?> skip(
      @PathVariable("gameId") UUID gameId,
      @RequestParam("playerId") UUID playerId
  ) {
    return move(gameId, playerId, null);
  }
}
