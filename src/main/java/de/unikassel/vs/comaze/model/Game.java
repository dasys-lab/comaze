package de.unikassel.vs.comaze.model;

import java.beans.Transient;
import java.util.*;

public class Game {
  private final GameConfig config;
  private final String name;
  private final UUID uuid = UUID.randomUUID();
  private Int2D agentPosition;
  private int usedMoves = 0;
  private final List<Color> reachedGoals = new ArrayList<>();
  private final List<Player> players = new ArrayList<>();
  private int currentPlayerIndex = 0;
  private int bonusMoves;

  public Game(String name, GameConfig config) {
    this.name = name;
    this.config = config;
    this.agentPosition = config.getAgentStartPosition();
  }

  @Transient
  public int getFreeActions() {
    return Direction.values().length - getAssignedActions().size();
  }

  @Transient
  public Set<Direction> getAssignedActions() {
    Set<Direction> assignedActions = new HashSet<>();
    for (Player player : players) {
      assignedActions.addAll(player.getActions());
    }
    return assignedActions;
  }

  @Transient
  public Direction getUnassignedAction() {
    List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
    Set<Direction> assignedActions = getAssignedActions();
    directions.removeAll(assignedActions);
    if (directions.isEmpty()) {
      return null;
    }
    Collections.shuffle(directions);
    return directions.get(0);
  }

  public Integer getMaxMoves() {
    Integer initialMaxMoves = getConfig().getInitialMaxMoves();
    if (initialMaxMoves == null) {
      return null;
    }
    return initialMaxMoves + getBonusMoves();
  }

  public boolean getMayStillMove() {
    Integer maxMoves = getMaxMoves();
    if (maxMoves == null) {
      return true;
    }
    return getUsedMoves() < maxMoves;
  }

  public boolean getGameWon() {
    int goals = getConfig().getGoals().size();
    int reachedGoals = getReachedGoals().size();
    return reachedGoals >= goals;
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

  public List<Color> getReachedGoals() {
    return reachedGoals;
  }

  public int getUsedMoves() {
    return usedMoves;
  }
}
