package de.unikassel.vs.magicmaze.model;

public class BonusTime {
  private final Int2D position;
  private final int amount;

  public BonusTime(Int2D position, int amount){
    this.position = position;
    this.amount = amount;
  }

  public Int2D getPosition() {
    return position;
  }

  public int getAmount() {
    return amount;
  }
}
