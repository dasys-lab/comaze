package de.unikassel.vs.comaze.model;

public class GameState {
  private Game game;

  public GameState(Game game) {
    this.game = game;
  }

  public boolean getStarted() {
    return game.getUnassignedActions() == 0;
  }

  public boolean getOver() {
    return getWon() || getLost();
  }

  public boolean getRunning() {
    return getStarted() && !getOver();
  }

  public boolean getWon() {
    return game.getUnreachedGoals().isEmpty();
  }

  public boolean getLost() {
    return !getWon() && !game.getMayStillMove();
  }
}
