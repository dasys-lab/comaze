package de.unikassel.vs.comaze.model;

import java.util.UUID;

public class PlayerDTO {
  private final String name;
  private final UUID uuid;

  public PlayerDTO(Player player) {
    this.name = player.getName();
    this.uuid = player.getUuid();
  }

  public String getName() {
    return name;
  }

  public UUID getUuid() {
    return uuid;
  }
}
