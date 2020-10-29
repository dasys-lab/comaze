package de.unikassel.vs.magicmaze.model;

import java.util.Objects;

public class Goal {
  private final Int2D position;
  private final Color color;

  public Goal(Int2D position, Color color) {
    this.position = position;
    this.color = color;
  }

  public Int2D getPosition() {
    return position;
  }

  public Color getColor() {
    return color;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Goal goal = (Goal) o;
    return position.equals(goal.position) &&
        color == goal.color;
  }

  @Override
  public int hashCode() {
    return Objects.hash(position, color);
  }
}
