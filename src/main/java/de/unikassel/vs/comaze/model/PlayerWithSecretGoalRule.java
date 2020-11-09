package de.unikassel.vs.comaze.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerWithSecretGoalRule {
  private final String name;
  private final UUID uuid;
  private final Set<Direction> directions = new HashSet<>();
  private String lastAction;
  private SecretGoalRule secretGoalRule;

  public PlayerWithSecretGoalRule(Player player) {
    name = player.getName();
    uuid = player.getUuid();
    directions.addAll(player.getDirections());
    lastAction = player.getLastAction();
  }

  public SecretGoalRule getSecretGoalRule() {
    return secretGoalRule;
  }

  public Set<Direction> getDirections() {
    return directions;
  }

  public Set<String> getActions() {
    Set<String> directionsWithNull = directions.stream().map(Direction::name).collect(Collectors.toSet());
    directionsWithNull.add(Direction.SKIP);
    return directionsWithNull;
  }

  public UUID getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }

  public String getLastAction() {
    return lastAction;
  }

  public void setSecretGoalRule(SecretGoalRule secretGoalRule) {
    this.secretGoalRule = secretGoalRule;
  }
}
