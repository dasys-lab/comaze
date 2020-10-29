package de.unikassel.vs.comaze.model;

import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Wall wall = (Wall) o;
    return position.equals(wall.position) &&
        direction == wall.direction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(position, direction);
  }
}
