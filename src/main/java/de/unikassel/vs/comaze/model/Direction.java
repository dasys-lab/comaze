package de.unikassel.vs.comaze.model;

import java.beans.Transient;

public enum Direction {
  LEFT(-1, 0),
  RIGHT(1, 0),
  UP(0, -1),
  DOWN(0, 1);

  public static final String SKIP = "SKIP";

  private final Int2D dir;

  Direction(int dirX, int dirY) {
    this.dir = new Int2D(dirX, dirY);
  }

  @Transient
  public Int2D getDir() {
    return dir;
  }

  public static Direction get(String str) {
    if (str.equals(SKIP)) {
      return null;
    } else {
      return valueOf(str);
    }
  }
}