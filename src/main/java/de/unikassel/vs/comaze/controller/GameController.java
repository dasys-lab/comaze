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
      summary = "Get a list of games",
      description = "Gets a list of games with their UUIDs, state and attending players.",
      hidden = true,
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "A list of games",
              content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = GameDTO.class)
              )
          )
      }
  )
  @GetMapping("/gamesss")
  public ResponseEntity<?> getGames() {
    List<Object> games = this.games.values().stream()
        .map(GameDTO::new)
        .collect(Collectors.toList());
    return ResponseEntity.ok(games);
  }

  @Operation(
      summary = "Search a game by a player's name",
      description = "Looks for an open game with the provided player waiting",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "A game object",
              content = {
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = Game.class)
                  )
              }
          )
      }
  )
  @GetMapping("/game/byPlayerName")
  public ResponseEntity<?> attendGame(
      @Parameter(description = "The name of a player that is already attending an open game")
      @RequestParam(value = "playerName")
          String playerName
  ) {
    Optional<Game> game = games.values().stream()
        .filter(gameValue -> !gameValue.getState().getStarted())
        .filter(gameValue -> gameValue.getPlayers().stream()
            .anyMatch(player -> player.getName().equals(playerName))
        )
        .max(Comparator.comparing(Game::getStayingAliveSince));

    if (game.isEmpty()) {
      return ResponseEntity.badRequest().body("No such game exists");
    } else {
      return ResponseEntity.ok(game.get());
    }
  }

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
          int numOfPlayerSlots,

      @Parameter(description = "Set a Limit for the game's action rate (e. g. if you want to watch AIs play)")
      @RequestParam(required = false)
          Double actionRateLimit
  ) {
    GameConfig config = GameConfigCreator.createLevel(level);
    if (config == null) {
      return ResponseEntity.badRequest().body("There is no such level");
    }

    Game game = new Game(name, config, numOfPlayerSlots, actionRateLimit);
    game.stayAlive();
    games.put(game.getUuid(), game);
    log.info("Game created: " + game);
    return ResponseEntity.ok(game);
  }

  @Operation(
      summary = "Attend a game",
      description = "Creates a new player and attends the provided game.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The newly created player object. If the game you attend has secret goal rules enabled, you receive the secret rule for your player within this response. You cannot request it later, so make sure to remember it.",
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
      log.info(playerName + " attempts to attend a game that does not exist: " + gameId);
      return ResponseEntity.badRequest().body("The game does not exist");
    }

    game.stayAlive();

    int unassignedPlayerSlots = game.getNumOfPlayerSlots() - game.getPlayers().size();
    int unassignedDirections = game.getUnassignedDirections();

    if (unassignedDirections == 0) {
      log.info(playerName + " attempts to attend a game that is full: " + game);
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
      SecretGoalRule rule = game.getUnassignedSecretGoalRule();
      rule.setPlayer(player);
      player.setSecretGoalRule(rule);
    }

    log.info(player + " is attending the game: " + game);
    return ResponseEntity.ok(player);
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
          String actionStr,

      @Parameter(
          description = "Send a single symbol out of a pre-defined set as a message that can be seen and interpreted by other players"
      )
      @RequestParam(value = "symbolMessage", required = false)
          SymbolMessage symbolMessage
  ) {
    Game game = games.get(gameId);

    if (game == null) {
      log.info("Someone (" + playerId + ") attempts to make a move in a game that does not exist: " + gameId);
      return ResponseEntity.badRequest().body("The game does not exist");
    }

    game.waitForActionRateLimit();
    game.stayAlive();

    Optional<Player> optPlayer = game.getPlayers().stream().filter(player -> player.getUuid().equals(playerId)).findFirst();
    if (optPlayer.isEmpty()) {
      log.info("Someone (" + playerId + ") attempts to make a move in a game that they are not attending: " + game);
      return ResponseEntity.badRequest().body("Player does not exist");
    }
    Player player = optPlayer.get();

    if (!game.getState().getStarted()) {
      log.info(player + " attempts to make a move in a game that has not started yet: " + game);
      return ResponseEntity.badRequest().body("The game has not started yet because there are not enough players");
    }

    if (!game.getCurrentPlayer().equals(player)) {
      log.debug(player + " attempts to make a move although it is not their turn in the game: " + game);
      return ResponseEntity.badRequest().body("It is not your turn");
    }

    Direction direction = Direction.get(actionStr);

    if (direction != null) { // direction == null => skipping move
      if (!player.getDirections().contains(direction)) {
        log.debug(player + " attempts to make a move but is not allowed to go " + actionStr + " in the game: " + game);
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
        log.info(player + " attempts to make a move in a game that is already over: " + game);
        return ResponseEntity.badRequest().body("Game over");
      }

      game.setAgentPosition(newPosition);

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
    player.setLastSymbolMessage(symbolMessage);
    game.increaseUsedMoves();
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

    deadGames.forEach(game -> log.info("Killing game: " + game));

  }
}
