package de.unikassel.vs.magicmaze.model;

import java.util.HashSet;
import java.util.Set;

public class GameConfig {
  private final Int2D arenaSize;
  private final Set<Wall> walls = new HashSet<>();
  private final Set<Goal> goals = new HashSet<>();
  private final Set<BonusTime> bonusTimes = new HashSet<>();
  private final Int2D agentStartPosition;

  GameConfig(Int2D arenaSize) {
    this(arenaSize, new Int2D(arenaSize.getX() / 2, arenaSize.getY() / 2));
  }

  GameConfig(Int2D arenaSize, Int2D agentStartPosition) {
    this.arenaSize = arenaSize;
    this.agentStartPosition = agentStartPosition;
  }

  public Wall addWall(Int2D position, Direction direction) {
    boolean fitsX = position.getX() < agentStartPosition.getX();
    boolean fitsY = position.getY() < agentStartPosition.getY();
    if (fitsX && fitsY) {
      Wall wall = new Wall(position, direction);
      walls.add(wall);
      return wall;
    } else {
      throw new IllegalArgumentException("Invalid position for wall");
    }
  }

  public Int2D getArenaSize() {
    return arenaSize;
  }

  public Set<Wall> getWalls() {
    return walls;
  }

  public Set<Goal> getGoals() {
    return goals;
  }

  public Set<BonusTime> getBonusTimes() {
    return bonusTimes;
  }

  public Int2D getAgentStartPosition() {
    return agentStartPosition;
  }
}