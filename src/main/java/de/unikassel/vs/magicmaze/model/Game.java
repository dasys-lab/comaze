package de.unikassel.vs.magicmaze.model;

import java.util.UUID;

public class Game {
  private final GameConfig config;
  private final String name;
  private final UUID uuid = UUID.randomUUID();
  private Int2D agentPosition;

  public Game(String name, GameConfig config) {
    this.name = name;
    this.config = config;
    this.agentPosition = config.getAgentStartPosition();
  }

  public String getName() {
    return name;
  }

  public UUID getUuid() {
    return uuid;
  }

  public GameConfig getConfig() {
    return config;
  }
}
