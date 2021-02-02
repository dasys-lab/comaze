package de.unikassel.vs.comaze.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.Transient;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Player {
  private final String name;
  private UUID uuid = UUID.randomUUID();
  private final Set<Direction> directions = new HashSet<>();
  private String lastAction;
  private String predictedAction;
  private String predictedGoal;
  private SymbolMessage lastSymbolMessage;
  private SecretGoalRule secretGoalRule;
  private SecretGoalRule explicitSecretGoalRule;

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

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
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

  public String getPredictedAction() {
    return predictedAction;
  }

  public void setPredictedAction(String predictedAction) {
    this.predictedAction = predictedAction;
  }

  public String getPredictedGoal() {
    return predictedGoal;
  }

  public void setPredictedGoal(String predictedGoal) {
    this.predictedGoal = predictedGoal;
  }

  public SymbolMessage getLastSymbolMessage() {
    return lastSymbolMessage;
  }

  public void setLastSymbolMessage(SymbolMessage lastSymbolMessage) {
    this.lastSymbolMessage = lastSymbolMessage;
  }

  @Transient
  public SecretGoalRule getSecretGoalRule() {
    return secretGoalRule;
  }

  public void setSecretGoalRule(SecretGoalRule secretGoalRule) {
    this.secretGoalRule = secretGoalRule;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("secretGoalRule")
  public SecretGoalRule getExplicitSecretGoalRule() {
    return explicitSecretGoalRule;
  }

  public void setExplicitSecretGoalRule(SecretGoalRule explicitSecretGoalRule) {
    this.explicitSecretGoalRule = explicitSecretGoalRule;
  }

  // clones the player and sets the otherwise unused explicitSecretGoalRule property. this way, the secret goal rule is only serialized when explicitly calling this method since the original secretGoalRule property is transient.
  public Player withSecretGoalRule() {
    Player player = new Player(name);
    player.setUuid(uuid);
    player.getDirections().addAll(directions);
    player.setLastAction(lastAction);
    player.setPredictedAction(predictedAction);
    player.setLastSymbolMessage(lastSymbolMessage);
    player.setExplicitSecretGoalRule(secretGoalRule);
    return player;
  }

  @Transient
  public String toString() {
    return name != null && !name.isEmpty() ? name + "/" + uuid : uuid.toString();
  }
}
