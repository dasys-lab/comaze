package de.unikassel.vs.comaze.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerWithSecretGoalRule {
  private final String name;

  private final UUID uuid;
  private final Set<Direction> actions = new HashSet<>();
  private Direction lastAction = null;
  private SecretGoalRule secretGoalRule;

  public PlayerWithSecretGoalRule(Player player) {
    name = player.getName();
    uuid = player.getUuid();
    actions.addAll(player.getActions());
    lastAction = player.getLastAction();
  }

  public String getName() {
    return name;
  }

  public UUID getUuid() {
    return uuid;
  }

  public Set<Direction> getActions() {
    return actions;
  }

  public Direction getLastAction() {
    return lastAction;
  }

  public SecretGoalRule getSecretGoalRule() {
    return secretGoalRule;
  }

  public void setSecretGoalRule(SecretGoalRule secretGoalRule) {
    this.secretGoalRule = secretGoalRule;
  }
}
