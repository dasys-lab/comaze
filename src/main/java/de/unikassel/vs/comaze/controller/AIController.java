package de.unikassel.vs.comaze.controller;

import io.swagger.v3.oas.annotations.Hidden;
import net.lingala.zip4j.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@Hidden
public class AIController {
  @Value("${uploadsPath}")
  private String uploadsPath;

  @Value("${python}")
  private String python;

  Logger log = LoggerFactory.getLogger(AIController.class);

  @PostMapping("/ai")
  public void launchAi(
      @RequestParam
          UUID gameId,

      @RequestParam
          String ai,

      @RequestParam(required = false, defaultValue = "false")
          boolean fresh
  ) throws IOException, InterruptedException {
    if (!getAis().contains(ai)) {
      throw new IllegalArgumentException("Refusing to launch AI " + ai);
    }

    String extractedPath = Paths.get(uploadsPath).resolve(ai).toFile().getAbsolutePath();

    if (fresh) {
      ZipFile zipFile = new ZipFile(getAiZip(ai).toFile());
      zipFile.extractAll(extractedPath);
    }

    ProcessBuilder processBuilder;
    switch (ai) {
      case "Die_Ratten":
        processBuilder = new ProcessBuilder(python, Paths.get(extractedPath).resolve("main.py").toFile().getAbsolutePath(), gameId.toString());
        break;
      case "Team_DGL":
        processBuilder = new ProcessBuilder(python, Paths.get(extractedPath).resolve("main.py").toFile().getAbsolutePath(), "-j", "-i", gameId.toString());
        break;
      case "Fantastic_3_4":
        processBuilder = new ProcessBuilder(python, Paths.get(extractedPath).resolve("Main.py").toFile().getAbsolutePath(), "--gameId", gameId.toString());
        break;
      case "Team_Knusprig":
        processBuilder = new ProcessBuilder(python, Paths.get(extractedPath).resolve("teamwork").resolve("main.py").toFile().getAbsolutePath(), gameId.toString());
        break;
      default:
        throw new IllegalArgumentException("Unable to launch AI " + ai);
    }

    log.info("Launching AI " + ai);
    Process process = processBuilder.start();
    process.waitFor();
    log.info("AI " + ai + " terminated: " + process.exitValue());
  }

  @GetMapping("/ai")
  public List<String> getAis() throws IOException {
    return Files.list(Paths.get(uploadsPath))
        .filter(path -> path.toString().endsWith(".zip"))
        .map(path -> {
          String fileName = path.getFileName().toString();
          String[] split = fileName.split("-");
          return split[1];
        })
        .collect(Collectors.toList());
  }

  private Path getAiZip(String ai) throws IOException {
    return Files.list(Paths.get(uploadsPath))
        .filter(path -> path.toString().endsWith(".zip"))
        .filter(path -> path.toString().contains(ai))
        .findAny()
        .orElseThrow(IllegalArgumentException::new);
  }
}
