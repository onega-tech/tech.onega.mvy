package tech.onega.mvy.app;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.bval.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.onega.mvy.mvn.MvnLauncher;
import tech.onega.mvy.mvn.MvnPomXmlFactory;
import tech.onega.mvy.mvy.MvyModelFactory;
import tech.onega.mvy.utils.Check;
import tech.onega.mvy.utils.fs.StdFsPath;

public class AppService implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppService.class);

  @Override
  public void close() {
  }

  public void createPomXml(final String mvyYamlPath, final String pomXmlPath) {
    try {
      final var mvyYamlFile = new File(new File("."), "mvy.yaml");
      final var pomXmlFile = StringUtils.isBlank(pomXmlPath)
        ? new File(new File("."), "pom.xml")
        : new File(pomXmlPath);
      //
      Check.isTrue(mvyYamlFile.exists(), "File not exist %s", mvyYamlFile);
      //
      LOGGER.info("GEN pom.xml - src:{} -> dest:{}", mvyYamlFile.getCanonicalPath(), pomXmlFile);
      final var pomXml = this.createPomXml(mvyYamlFile.toURI());
      Files.write(pomXmlFile.toPath(), pomXml.getBytes(StandardCharsets.UTF_8));
      LOGGER.info("DONE");
    }
    catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public String createPomXml(final URI yamlEndpoint) {
    final var project = MvyModelFactory.createProjectFromYaml(yamlEndpoint);
    final var mvnPomXmlFactory = new MvnPomXmlFactory(project);
    return mvnPomXmlFactory.buildPomXml();
  }

  public int runMvn(final List<String> args) {
    final var envs = new LinkedHashMap<String, String>(System.getenv());
    envs.put(MvnLauncher.ENV_NAME_JAVA_HOME, "/opt/java/jdk17g");
    envs.put(MvnLauncher.ENV_NAME_MAVEN_HOME, "/opt/maven");
    final var workDirPath = StdFsPath.of(".");
    return MvnLauncher.launch(workDirPath, envs, args);
  }

}
