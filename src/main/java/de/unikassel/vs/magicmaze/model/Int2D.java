package de.unikassel.vs.magicmaze.model;

import java.util.Objects;

public class Int2D {
  private int x;
  private int y;

  public Int2D(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public boolean fitsIn(Int2D that) {
    return this.getX() < that.getX()
        && this.getY() < that.getY()
        && this.getX() >= 0
        && this.getY() >= 0;
  }

  public Int2D plus(Int2D that) {
    return new Int2D(this.getX() + that.getX(), this.getY() + that.getY());
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Int2D int2D = (Int2D) o;
    return x == int2D.x &&
        y == int2D.y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }
}
