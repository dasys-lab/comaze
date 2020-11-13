package de.unikassel.vs.comaze.model;

import java.beans.Transient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Game {
  private final GameConfig config;
  private final GameState state;
  private final String name;
  private final UUID uuid = UUID.randomUUID();
  private Int2D agentPosition;
  private int usedMoves = 0;
  private final List<Goal> unreachedGoals = new ArrayList<>();
  private final List<BonusTime> unusedBonusTimes = new ArrayList<>();
  private final List<Player> players = new ArrayList<>();
  private final int numOfPlayerSlots;
  private int currentPlayerIndex = 0;
  private int bonusMoves;
  private final List<SecretGoalRule> secretGoalRules = new ArrayList<>();

  private LocalDateTime stayingAliveSince = LocalDateTime.now();
  private final static Duration TIME_TO_LIVE = Duration.ofMinutes(60);

  public Game(String name, GameConfig config, int numOfPlayerSlots) {
    this.name = name;
    this.config = config;
    this.numOfPlayerSlots = numOfPlayerSlots;
    this.state = new GameState(this);
    this.agentPosition = config.getAgentStartPosition();
    this.unreachedGoals.addAll(config.getGoals());
    this.unusedBonusTimes.addAll(config.getBonusTimes());
  }

  @Transient
  public int getUnassignedDirections() {
    return Direction.values().length - getAssignedDirections().size();
  }

  @Transient
  public Set<Direction> getAssignedDirections() {
    Set<Direction> assignedDirections = new HashSet<>();
    for (Player player : players) {
      assignedDirections.addAll(player.getDirections());
    }
    return assignedDirections;
  }

  @Transient
  public Direction getUnassignedDirection(List<Direction> preferredDirections) {
    List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
    Set<Direction> assignedDirections = getAssignedDirections();
    directions.removeAll(assignedDirections);

    // are there ANY directions left?
    if (directions.isEmpty()) {
      return null;
    }

    // are there preferred directions left?
    for (Direction preferredDirection : preferredDirections) {
      if (directions.contains(preferredDirection)) {
        return preferredDirection;
      }
    }

    Collections.shuffle(directions);
    return directions.get(0);
  }

  @Transient
  public SecretGoalRule getUnassignedSecretGoalRule() {
    // lazy initialize secretGoalRules
    if (secretGoalRules.isEmpty()) {
      // determine order of goals
      List<Goal> orderedGoals = new ArrayList<>(config.getGoals());
      Collections.shuffle(orderedGoals);

      // generate all possible goal rules
      for (int earlierGoalIndex = 0; earlierGoalIndex < orderedGoals.size() - 1; earlierGoalIndex++) {
        for (int laterGoalIndex = earlierGoalIndex + 1; laterGoalIndex < orderedGoals.size(); laterGoalIndex++) {
          secretGoalRules.add(new SecretGoalRule(orderedGoals.get(earlierGoalIndex), orderedGoals.get(laterGoalIndex)));
        }
      }
    }

    // find any unassigned rule
    Collections.shuffle(secretGoalRules);
    return secretGoalRules.stream()
        .filter(rule -> rule.getPlayer() == null)
        .findAny()
        .orElse(null);
  }

  @Transient
  public List<SecretGoalRule> getSecretGoalRules() {
    return secretGoalRules;
  }

  @Transient
  public List<SecretGoalRule> getAssignedSecretGoalRules() {
    return secretGoalRules.stream()
        .filter(rule -> rule.getPlayer() != null)
        .collect(Collectors.toList());
  }

  @Transient
  public Optional<SecretGoalRule> isSecretGoalRuleViolated() {
    return getAssignedSecretGoalRules().stream()
        .filter(rule ->
            getUnreachedGoals().contains(rule.getEarlierGoal()) && // earlier goal has _not_ been reached
                !getUnreachedGoals().contains(rule.getLaterGoal()) // later goal has been reached
        )
        .findAny();
  }

  public Integer getMaxMoves() {
    Integer initialMaxMoves = getConfig().getInitialMaxMoves();
    if (initialMaxMoves == null) {
      return null;
    }
    return initialMaxMoves + getBonusMoves();
  }

  public Integer getMovesLeft() {
    if (getConfig().getInitialMaxMoves() == null) {
      return null;
    }
    return getMaxMoves() - getUsedMoves();
  }

  public boolean getMayStillMove() {
    Integer maxMoves = getMaxMoves();
    if (maxMoves == null) {
      return true;
    }
    return getUsedMoves() < maxMoves;
  }

  public void setNextPlayer() {
    this.currentPlayerIndex++;
    if (this.currentPlayerIndex == players.size()) {
      this.currentPlayerIndex = 0;
    }
  }

  public void increaseUsedMoves() {
    this.usedMoves++;
  }

  public void addBonusMoves(int amount) {
    bonusMoves += amount;
  }

  public Player getCurrentPlayer() {
    if (players.isEmpty()) {
      return null;
    }
    return players.get(currentPlayerIndex);
  }

  @Transient
  public int getBonusMoves() {
    return bonusMoves;
  }

  public int getUsedMoves() {
    return usedMoves;
  }

  public void setBonusMoves(int bonusMoves) {
    this.bonusMoves = bonusMoves;
  }

  public String getName() {
    return name;
  }

  public UUID getUuid() {
    return uuid;
  }

  public GameConfig getConfig() {
    return config;
  }

  public Int2D getAgentPosition() {
    return agentPosition;
  }

  public void setAgentPosition(Int2D agentPosition) {
    this.agentPosition = agentPosition;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public List<Goal> getUnreachedGoals() {
    return unreachedGoals;
  }

  public List<BonusTime> getUnusedBonusTimes() {
    return unusedBonusTimes;
  }

  public GameState getState() {
    return state;
  }

  public int getNumOfPlayerSlots() {
    return numOfPlayerSlots;
  }

  public void stayAlive() {
    this.stayingAliveSince = LocalDateTime.now();
  }

  @Transient
  public boolean mayDie() {
    LocalDateTime deadline = stayingAliveSince.plus(TIME_TO_LIVE);
    return LocalDateTime.now().isAfter(deadline);
  }

  @Transient
  public String toString() {
    return name != null && !name.isEmpty() ? name + "/" + uuid : uuid.toString();
  }
}
