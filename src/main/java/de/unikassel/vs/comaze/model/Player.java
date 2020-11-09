package de.unikassel.vs.comaze.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Player {
  private final String name;
  private final UUID uuid = UUID.randomUUID();
  private final Set<Direction> directions = new HashSet<>();
  private String lastAction;

  public Player(String name) {
    this.name = name;
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

  public void setLastAction(String lastAction) {
    this.lastAction = lastAction;
  }
}
