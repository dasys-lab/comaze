package de.unikassel.vs.comaze.controller;

import de.unikassel.vs.comaze.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
public class LevelController {
  @Operation(
      summary = "Get a list of available levels",
      description = "Use these level names when creating games.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "List of available levels",
              content = {
                  @Content(
                      mediaType = "application/json",
                      examples = @ExampleObject("[\"1\",\"2\",\"3\",\"4\"]")
                  )
              }
          )
      }
  )
  @GetMapping("/levels")
  public Collection<String> getGames() {
    return GameConfigCreator.getLevels();
  }
}
