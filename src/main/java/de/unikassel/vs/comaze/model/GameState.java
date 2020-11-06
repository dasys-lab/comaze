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
    if (getWon()) {
      return false;
    }

    return !game.getMayStillMove() || game.isSecretGoalRuleViolated().isPresent();
  }

  public String getLostMessage() {
    if (!game.getMayStillMove()) {
      return "You have no moves left";
    } else if (game.isSecretGoalRuleViolated().isPresent()) {
      SecretGoalRule rule = game.isSecretGoalRuleViolated().get();
      return "The secret rule of " + rule.getPlayer().getName() + " has been violated:\n" +
          rule.getEarlierGoal().getColor() + " must be reached before " + rule.getLaterGoal().getColor();
    } else {
      return null;
    }
  }
}
