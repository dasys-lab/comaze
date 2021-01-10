package de.unikassel.vs.comaze.model;

import java.util.HashSet;
import java.util.Set;

public class WallGenerator {
  public static void generateWalls(GameConfig config, int amountOfRandomWalls) {
    Set<Wall> walls = config.getWalls();

    // generate random walls and check if solvable - if not, start over
    do {
      walls.clear();

      while (walls.size() < amountOfRandomWalls) {
        Int2D newPos = new Int2D(
            (int) (Math.random() * config.getArenaSize().getX()),
            (int) (Math.random() * config.getArenaSize().getY())
        );

        Direction newDirection = Math.random() < 0.5 ? Direction.RIGHT : Direction.DOWN;

        // do not create walls at the edge of the arena
        if (newPos.getX() == config.getArenaSize().getX() - 1 && newDirection.equals(Direction.RIGHT) ||
            newPos.getY() == config.getArenaSize().getY() - 1 && newDirection.equals(Direction.DOWN)) {
          continue;
        }

        walls.add(new Wall(newPos, newDirection));
      }
    }
    while (!isSolvable(config));
  }

  private static boolean isSolvable(GameConfig config) {
    Set<Int2D> reached = new HashSet<>(); // reachable, but still have to check neighbor positions
    Set<Int2D> unreached = new HashSet<>();

    for (int x = 0; x < config.getArenaSize().getX(); x++) {
      for (int y = 0; y < config.getArenaSize().getY(); y++) {
        unreached.add(new Int2D(x, y));
      }
    }

    reached.add(config.getAgentStartPosition());
    unreached.remove(config.getAgentStartPosition());

    while (!unreached.isEmpty() && !reached.isEmpty()) {
      Int2D reachedPosition = reached.stream().findAny().get();
      for (Direction direction : Direction.values()) {
        Int2D neighborPosition = reachedPosition.plus(direction.getDir());
        if (!config.hasWallBetween(reachedPosition, neighborPosition) && unreached.contains(neighborPosition)) {
          unreached.remove(neighborPosition);
          reached.add(neighborPosition);
        }
      }
      reached.remove(reachedPosition);
    }

    return unreached.isEmpty();
  }
}
