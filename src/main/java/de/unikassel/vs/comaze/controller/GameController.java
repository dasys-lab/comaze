package de.unikassel.vs.comaze.controller;

import de.unikassel.vs.comaze.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@EnableScheduling
public class GameController {
  Map<UUID, Game> games = new HashMap<>();
  Logger log = LoggerFactory.getLogger(GameController.class);

  @Operation(
      summary = "Get a game",
      description = "Gets a game object representing its configuration and current state.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The matching game object",
              content = {
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = Game.class)
                  )
              }
          )
      }
  )
  @GetMapping("/game/{gameId}")
  public ResponseEntity<?> getGame(
      @Parameter(description = "UUID of the game")
      @PathVariable("gameId")
          UUID gameId
  ) {
    if (games.containsKey(gameId)) {
      return ResponseEntity.ok(games.get(gameId));
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(
      summary = "Create a game",
      description = "Creates a new game and returns the game object.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The newly created game object",
              content = {
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = Game.class)
                  )
              }
          )
      }
  )
  @PostMapping("/game/create")
  public ResponseEntity<?> createGame(
      @Parameter(description = "A name for the game")
      @RequestParam(value = "name", required = false)
          String name,

      @Parameter(description = "The level you want to play")
      @RequestParam(value = "level", required = false, defaultValue = "3")
          String level,

      @Parameter(description = "The amount of players needed to play the game")
      @RequestParam(value = "numOfPlayerSlots", required = false, defaultValue = "2")
          int numOfPlayerSlots
  ) {
    GameConfig config = GameConfigCreator.createLevel(level);
    if (config == null) {
      return ResponseEntity.badRequest().body("There is no such level");
    }

    Game game = new Game(name, config, numOfPlayerSlots);
    game.stayAlive();
    games.put(game.getUuid(), game);
    return ResponseEntity.ok(game);
  }

  @Operation(
      summary = "Attend a game",
      description = "Creates a new player and attends the provided game.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The newly created player object",
              content = {
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = Player.class)
                  )
              }
          )
      }
  )
  @PostMapping("/game/{gameId}/attend")
  public ResponseEntity<?> attendGame(
      @Parameter(description = "The UUID of the game you wish to attend")
      @PathVariable("gameId")
          UUID gameId,

      @Parameter(description = "A name that allows other players to identify you")
      @RequestParam(value = "playerName")
          String playerName,

      @Parameter(
          description = "Comma separated list of actions you prefer to play with",
          examples = {
              @ExampleObject(
                  name = "Prefer DOWN",
                  summary = "Prefer DOWN",
                  value = "DOWN",
                  description = "You will be assigned DOWN if available and a random action in a 2-player game."
              ),
              @ExampleObject(
                  name = "Prefer LEFT and RIGHT",
                  summary = "Prefer LEFT and RIGHT",
                  value = "LEFT,RIGHT",
                  description = "You will be assigned LEFT and RIGHT if available."
              ),
              @ExampleObject(
                  name = "No preferred actions",
                  summary = "No preferred actions",
                  value = "",
                  description = "You will be assigned a random set of actions."
              )
          }
      )
      @RequestParam(value = "preferredDirections", required = false, defaultValue = "")
          String preferredDirectionsStr
  ) {
    Game game = games.get(gameId);

    if (game == null) {
      return ResponseEntity.badRequest().body("The game does not exist");
    }

    game.stayAlive();

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

  @Operation(
      summary = "Make a move",
      description = "Makes the provided player move in the provided game.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The updated game object",
              content = {
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = Game.class)
                  )
              }
          )
      }
  )
  @PostMapping("/game/{gameId}/move")
  public ResponseEntity<?> move(
      @Parameter(description = "The UUID of the game")
      @PathVariable("gameId")
          UUID gameId,

      @Parameter(description = "The UUID of the player")
      @RequestParam("playerId")
          UUID playerId,

      @Parameter(
          description = "The direction you want the player to move or SKIP",
          examples = {
              @ExampleObject(
                  name = "Direction",
                  summary = "Direction",
                  value = "DOWN",
                  description = "You will move down"
              ),
              @ExampleObject(
                  name = "SKIP",
                  summary = "SKIP",
                  value = "SKIP",
                  description = "You will end your turn without moving"
              )
          }
      )
      @RequestParam("action")
          String actionStr
  ) {
    Game game = games.get(gameId);

    if (game == null) {
      return ResponseEntity.badRequest().body("The game does not exist");
    }

    game.stayAlive();

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

  @Scheduled(fixedRate = 60000)
  public void cleanupGames() {
    Set<UUID> deadGames = games.keySet().stream()
        .filter(gameId -> games.get(gameId).mayDie())
        .collect(Collectors.toSet());
    deadGames
        .forEach(gameId -> games.remove(gameId));

    if (!deadGames.isEmpty()) {
      log.info("Killing " + deadGames.size() + " game(s)");
    }
  }
}
