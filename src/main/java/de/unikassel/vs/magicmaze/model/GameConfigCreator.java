package de.unikassel.vs.magicmaze.model;

public class GameConfigCreator {
  private final static int NUM_OF_FIELDS = 7;
  private static final int INITIAL_MAX_MOVES = 30;
  private final static int AMOUNT_OF_BONUS_MOVES = 20;
  private int amountOfBonusMoves;

  public static GameConfig createLevel1() {
    GameConfig config = new GameConfig(new Int2D(NUM_OF_FIELDS, NUM_OF_FIELDS));
    config.addGoal(new Int2D(1, 1), Color.RED);
    config.addGoal(new Int2D(5, 1), Color.YELLOW);
    config.addGoal(new Int2D(1, 5), Color.GREEN);
    config.addGoal(new Int2D(5, 5), Color.BLUE);
    return config;
  }

  public static GameConfig createLevel2() {
    GameConfig config = createLevel1();
    config.addWall(new Int2D(2, 2), Direction.RIGHT);
    config.addWall(new Int2D(2, 4), Direction.RIGHT);
    config.addWall(new Int2D(2, 5), Direction.RIGHT);
    config.addWall(new Int2D(5, 2), Direction.RIGHT);
    config.addWall(new Int2D(4, 3), Direction.RIGHT);
    config.addWall(new Int2D(3, 2), Direction.DOWN);
    config.addWall(new Int2D(5, 2), Direction.DOWN);
    config.addWall(new Int2D(4, 3), Direction.DOWN);
    return config;
  }

  public static GameConfig createLevel3() {
    GameConfig config = createLevel2();
    config.setInitialMaxMoves(INITIAL_MAX_MOVES);
    config.addBonusTime(new Int2D(3, 0), AMOUNT_OF_BONUS_MOVES);
    config.addBonusTime(new Int2D(3, 6), AMOUNT_OF_BONUS_MOVES);
    return config;
  }
}
