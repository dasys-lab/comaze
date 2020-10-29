package de.unikassel.vs.magicmaze.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Player {
  private String name;
  private final UUID uuid = UUID.randomUUID();
  private final Set<Direction> actions = new HashSet<>();
  private Direction lastAction = null;

  public Player(String name) {
    this.name = name;
  }

  public Set<Direction> getActions() {
    return actions;
  }

  public UUID getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }

  public Direction getLastAction() {
    return lastAction;
  }

  public void setLastAction(Direction lastAction) {
    this.lastAction = lastAction;
  }
}
