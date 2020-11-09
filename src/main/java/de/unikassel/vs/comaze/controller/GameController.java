package de.unikassel.vs.comaze.controller;

import de.unikassel.vs.comaze.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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
      @RequestParam(value = "level", required = false, defaultValue = "3") String level,
      @RequestParam(value = "numOfPlayerSlots", required = false, defaultValue = "2") int numOfPlayerSlots
  ) {
    GameConfig config = GameConfigCreator.createLevel(level);
    if (config == null) {
      return ResponseEntity.badRequest().body("There is no such level");
    }

    Game game = new Game(name, config, numOfPlayerSlots);
    games.put(game.getUuid(), game);
    return ResponseEntity.ok(game);
  }

  @PostMapping("/game/{gameId}/attend")
  public ResponseEntity<?> attendGame(
      @PathVariable("gameId") UUID gameId,
      @RequestParam(value = "playerName") String playerName,
      @RequestParam(value = "preferredDirections", required = false, defaultValue = "") String preferredDirectionsStr
  ) {
    Game game = games.get(gameId);

    if (game == null) {
      return ResponseEntity.badRequest().body("The game does not exist");
    }

    int unassignedPlayerSlots = game.getNumOfPlayerSlots() - game.getPlayers().size();
    int unassignedDirections = game.getUnassignedDirections();

    if (unassignedDirections == 0) {
      return ResponseEntity.badRequest().body("The game is full");
    }

    int directionsToAssign = unassignedDirections / unassignedPlayerSlots;

    Player player = new Player(playerName);
    game.getPlayers().add(player);

    List<Direction> preferredDirections = Arrays.stream(preferredDirectionsStr.split(","))
        .filter(str -> !str.isBlank())
        .map(Direction::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    for (int i = 0; i < directionsToAssign; i++) {
      player.getDirections().add(game.getUnassignedDirection(preferredDirections));
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
      @RequestParam("action") String actionStr
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

    Direction direction = Direction.get(actionStr);

    if (direction != null) { // direction == null => skipping move
      if (!player.getDirections().contains(direction)) {
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
    player.setLastAction(direction != null ? direction.name() : Direction.SKIP);
    game.setNextPlayer();

    return ResponseEntity.ok().body(game);
  }
}
