package de.unikassel.vs.magicmaze.model;

public class Wall {
  private final Int2D position;
  private final Direction direction;

  public Wall(Int2D position, Direction direction) {
    this.position = position;
    this.direction = direction;
  }

  public Int2D getPosition() {
    return position;
  }

  public Direction getDirection() {
    return direction;
  }
}
