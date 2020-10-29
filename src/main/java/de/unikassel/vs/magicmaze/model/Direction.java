package de.unikassel.vs.magicmaze.model;

import java.beans.Transient;

public enum Direction {
  LEFT(-1, 0),
  RIGHT(1, 0),
  UP(0, -1),
  DOWN(0, 1);

  private final Int2D dir;

  Direction(int dirX, int dirY) {
    this.dir = new Int2D(dirX, dirY);
  }

  @Transient
  public Int2D getDir() {
    return dir;
  }
}