package de.unikassel.vs.magicmaze.model;

public class GameConfigCreator {
  private final static int NUM_OF_FIELDS = 7;

  public static GameConfig createLevel1() {
    GameConfig config = new GameConfig(new Int2D(NUM_OF_FIELDS, NUM_OF_FIELDS));
    return config;
  }

  public static GameConfig createLevel2() {
    GameConfig config = new GameConfig(new Int2D(NUM_OF_FIELDS, NUM_OF_FIELDS));
    config.addWall(new Int2D(20, 2), Direction.HORIZONTAL);
    return config;
  }
}
