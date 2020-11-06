package de.unikassel.vs.comaze.model;

import java.beans.Transient;

public class SecretGoalRule {
  private final Goal earlierGoal;
  private final Goal laterGoal;
  private Player player;

  public SecretGoalRule(Goal earlierGoal, Goal laterGoal) {
    this.earlierGoal = earlierGoal;
    this.laterGoal = laterGoal;
  }

  public Goal getEarlierGoal() {
    return earlierGoal;
  }

  public Goal getLaterGoal() {
    return laterGoal;
  }

  @Transient
  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }
}
