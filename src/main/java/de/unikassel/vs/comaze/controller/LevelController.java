package de.unikassel.vs.comaze.controller;

import de.unikassel.vs.comaze.model.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
public class LevelController {
  @GetMapping("/levels")
  public Collection<String> getGames() {
    return GameConfigCreator.getLevels();
  }
}
