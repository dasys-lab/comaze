package de.unikassel.vs.comaze.model;

import java.util.HashSet;
import java.util.Set;

public class GameConfig {
  private final Int2D arenaSize;
  private final Set<Wall> walls = new HashSet<>();
  private final Set<Goal> goals = new HashSet<>();
  private final Set<BonusTime> bonusTimes = new HashSet<>();
  private final Int2D agentStartPosition;
  private Integer initialMaxMoves;

  GameConfig(Int2D arenaSize) {
    this(arenaSize, new Int2D(arenaSize.getX() / 2, arenaSize.getY() / 2));
  }

  GameConfig(Int2D arenaSize, Int2D agentStartPosition) {
    this.arenaSize = arenaSize;
    this.agentStartPosition = agentStartPosition;
  }

  public void addWall(Int2D position, Direction direction) {
    if (!position.fitsIn(arenaSize)) {
      throw new IllegalArgumentException("Invalid position for wall");
    }

    walls.add(new Wall(position, direction));
  }

  public void addGoal(Int2D position, Color color) {
    if (!position.fitsIn(arenaSize)) {
      throw new IllegalArgumentException("Invalid position for goal");
    }

    goals.add(new Goal(position, color));
  }


  public void addBonusTime(Int2D position, int amount) {
    if (!position.fitsIn(arenaSize)) {
      throw new IllegalArgumentException("Invalid position for bonus time");
    }

    bonusTimes.add(new BonusTime(position, amount));
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

  public boolean hasWallBetween(Int2D pos1, Int2D pos2) {
    int deltaX = pos1.getX() - pos2.getX();
    int deltaY = pos1.getY() - pos2.getY();

    if (Math.abs(deltaX) + Math.abs(deltaY) != 1) { // FIXME also true for some diagonal pairs
      throw new IllegalArgumentException("Cannot check for walls between fields that are not adjacent");
    }

    Int2D wallPos = new Int2D(
        Math.min(pos1.getX(), pos2.getX()),
        Math.min(pos1.getY(), pos2.getY())
    );

    Direction wallDirection = deltaX != 0 ? Direction.RIGHT : Direction.DOWN;

    return walls.stream()
        .filter(wall -> wall.getPosition().equals(wallPos))
        .anyMatch(wall -> wall.getDirection().equals(wallDirection));
  }

  public Integer getInitialMaxMoves() {
    return initialMaxMoves;
  }

  public void setInitialMaxMoves(Integer initialMaxMoves) {
    this.initialMaxMoves = initialMaxMoves;
  }
}
