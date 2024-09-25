package tech.onega.mvy.app;

import java.util.ArrayList;
import java.util.Arrays;
import tech.onega.mvy.utils.Console;

public class AppMain {

  private static int gen(final String[] args) {
    if (args.length < 2) {
      Console.log("""

Usage:
  gen [in-file] [out-file]

Examples:
  gen .
      use mvy.xml in current directory and output to pom.xml in current directory

  gen mvy.yaml pom2.xml
      use mvy.xml in current directory and output to pom2.xml in current directory

  gen /tmp/mvy.yaml /tmp/pom.xml
      use /tmp/mvy.xml in current directory and output to /tmp/pom.xml in current directory
""");
      return 1;
    }
    final var inFile = args.length < 2 ? "" : args[1].trim();
    final var outFile = args.length < 3 ? "" : args[2].trim();
    try (var appService = new AppService()) {
      appService.createPomXml(inFile, outFile);
    }
    return 0;
  }

  private static int help(final String[] args) {
    Console.log("""

Usage:
  help     - generate current help
  version  - show app version
  gen      - generate pom.xml from mvy.yaml
  mvn      - run maven
""");
    return 0;
  }

  public static void main(final String[] args) {
    final var action = args.length < 1 ? "help" : args[0];
    final var exitCode = switch (action) {
      case "gen" -> gen(args);
      case "version" -> version(args);
      case "mvn" -> mvn(args);
      default -> help(args);
    };
    System.exit(exitCode);
  }

  private static int mvn(final String[] args) {
    if (args.length < 2) {
      Console.log("""

Usage:
  mvn [args]

Examples:
  mvn clean
      Run mvn clean task
""");
      return 1;
    }
    final var mvnArgs = args.length < 2 ? new ArrayList<String>() : Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
    try (var appService = new AppService()) {
      return appService.runMvn(mvnArgs);
    }
  }

  private static int version(final String[] args) {
    Console.logFormat("Mvy version: %s", AppConfig.VERSION);
    return 0;
  }

}
