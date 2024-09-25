package tech.onega.mvy.mvn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.onega.mvy.utils.Check;
import tech.onega.mvy.utils.StdError;
import tech.onega.mvy.utils.fs.StdFs;
import tech.onega.mvy.utils.fs.StdFsPath;

public class MvnLauncher {

  public static final String ENV_NAME_JAVA_HOME = "JAVA_HOME";

  public static final String ENV_NAME_MAVEN_HOME = "MAVEN_HOME";

  public static final String ENV_NAME_MAVEN_OPTS = "MAVEN_OPTS";

  private static final Logger LOGGER = LoggerFactory.getLogger(MvnLauncher.class);

  private static StdFsPath findFileInDir(final StdFsPath dir, final String name, final Predicate<String> matcher) {
    final var file = StdFs.dirList(dir)
      .filter(path -> StdFs.isFile(path, true))
      .filter(path -> matcher.test(path.name()))
      .findFirst()
      .orElse(null);
    Check.notNull(file, "Can't find file '%s' in dir '%s'", name, dir);
    return file;
  }

  public static int launch(final StdFsPath workDirPath, final Map<String, String> environments, final List<String> args) {
    try {
      final var workDir = resolveDir("WORK DIR", workDirPath);
      final var javaHomeDir = resolveDir("JAVA_HOME", environments.get(ENV_NAME_JAVA_HOME));
      final var mvnHomeDir = resolveDir("MAVEN_HOME", environments.get(ENV_NAME_MAVEN_HOME));
      final var javaBinFile = resolveFile("java bin file", javaHomeDir, "bin", "java");
      final var mvnJansiNativeDir = resolveDir("jansi native", mvnHomeDir, "lib", "jansi-native");
      final var mvnM2ConfFile = resolveFile("m2 conf", mvnHomeDir, "bin", "m2.conf");
      final var mvnBootDir = resolveDir("maven boot dir", mvnHomeDir, "boot");
      final var plexusClassWorldsFile = findFileInDir(mvnBootDir, "Plexus class worlds jar", fn -> fn.startsWith("plexus-classworlds") && fn.endsWith(".jar"));
      //
      final var command = new ArrayList<String>();
      command.add(javaBinFile.path());
      command.addAll(resolveMavenOptsArgs(environments));
      command.add("-classpath");
      command.add(plexusClassWorldsFile.path());
      command.add("-Dclassworlds.conf=" + mvnM2ConfFile.path());
      command.add("-Dmaven.home=" + mvnHomeDir.path());
      command.add("-Dlibrary.jansi.path=" + mvnJansiNativeDir.path());
      command.add("-Dmaven.multiModuleProjectDirectory=" + workDir.path());
      command.add("org.codehaus.plexus.classworlds.launcher.Launcher");
      command.addAll(args);
      //
      LOGGER.info("LAUNCH - {}", command.stream().collect(Collectors.joining(" ")));
      final var processBuilder = new ProcessBuilder();
      processBuilder
        .command(command)
        .environment().putAll(environments);
      processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
      processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      processBuilder.directory(new File(workDir.path()));
      final var process = processBuilder.start();
      return process.waitFor();
    }
    catch (final Exception e) {
      throw StdError.wrap(e);
    }
  }

  private static StdFsPath resolveDir(final String name, final Object... paths) {
    try {
      final var path = StdFsPath.of(paths);
      Check.isTrue(StdFs.isDir(path, true), " Dir is not exists '%s'", path);
      return path;
    }
    catch (final Exception e) {
      throw new StdError("Can't resolve '%s'".formatted(name), e);
    }
  }

  private static StdFsPath resolveFile(final String name, final Object... paths) {
    try {
      final var path = StdFsPath.of(paths);
      Check.isTrue(StdFs.isFile(path, true), "File is not exists '%s'", path);
      return path;
    }
    catch (final Exception e) {
      throw new StdError("Can't resolve '%s'".formatted(name), e);
    }
  }

  private static List<String> resolveMavenOptsArgs(final Map<String, String> environments) {
    final var result = new ArrayList<String>();
    final var mvnOpts = environments.get(ENV_NAME_MAVEN_OPTS);
    if (mvnOpts != null) {
      final var opts = mvnOpts.split(" ");
      for (var opt : opts) {
        opt = opt.trim();
        if (!opt.isBlank()) {
          result.add(opt);
        }
      }
    }
    return result;
  }

}
