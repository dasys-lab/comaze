package de.unikassel.vs.comaze.model;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameConfigCreator {
  private final static int NUM_OF_FIELDS = 7;
  private final static int INITIAL_MAX_MOVES_BEFORE_LEVEL_3 = 100;
  private final static int INITIAL_MAX_MOVES_LEVEL_3 = 20;
  private final static int AMOUNT_OF_RANDOM_WALLS = 14;
  private final static int AMOUNT_OF_BONUS_MOVES = 20;

  public static GameConfig createLevel1() {
    GameConfig config = new GameConfig(new Int2D(NUM_OF_FIELDS, NUM_OF_FIELDS));
    config.addGoal(new Int2D(1, 1), Color.RED);
    config.addGoal(new Int2D(5, 1), Color.YELLOW);
    config.addGoal(new Int2D(1, 5), Color.GREEN);
    config.addGoal(new Int2D(5, 5), Color.BLUE);
    config.setInitialMaxMoves(INITIAL_MAX_MOVES_BEFORE_LEVEL_3);
    return config;
  }

  public static GameConfig createLevel2() {
    GameConfig config = createLevel1();
    WallGenerator.generateWalls(config, AMOUNT_OF_RANDOM_WALLS);
    return config;
  }

  public static GameConfig createLevel3() {
    GameConfig config = createLevel2();
    config.setInitialMaxMoves(INITIAL_MAX_MOVES_LEVEL_3);
    config.addBonusTime(new Int2D(3, 0), AMOUNT_OF_BONUS_MOVES);
    config.addBonusTime(new Int2D(3, 6), AMOUNT_OF_BONUS_MOVES);
    return config;
  }

  public static GameConfig createLevel4() {
    GameConfig config = createLevel3();
    config.setHasSecretGoalRules(true);
    return config;
  }

  public static GameConfig createLevel(String level) {
    try {
      return (GameConfig) GameConfigCreator.class.getMethod("createLevel" + level).invoke(null);
    } catch (Exception e) {
      return null;
    }
  }

  public static List<String> getLevels() {
    return Arrays.stream(GameConfigCreator.class.getMethods())
        .filter(method -> !method.isAnnotationPresent(SecretLevel.class))
        .map(Method::getName)
        .filter(method -> method.startsWith("createLevel"))
        .map(method -> method.replace("createLevel", ""))
        .filter(method -> !method.isEmpty())
        .collect(Collectors.toList());
  }
}
