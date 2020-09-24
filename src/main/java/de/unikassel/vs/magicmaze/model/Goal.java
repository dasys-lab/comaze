package de.unikassel.vs.magicmaze.model;

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
}
