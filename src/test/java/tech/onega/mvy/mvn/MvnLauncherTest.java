package tech.onega.mvy.mvn;

import java.util.Arrays;
import java.util.LinkedHashMap;
import org.testng.annotations.Test;
import tech.onega.mvy.utils.fs.StdFsPath;

class MvnLauncherTest {

  @Test
  void testLaunch() {
    final var environments = new LinkedHashMap<String, String>(System.getenv());
    environments.put(MvnLauncher.ENV_NAME_JAVA_HOME, "/opt/java/jdk17g");
    environments.put(MvnLauncher.ENV_NAME_MAVEN_HOME, "/opt/maven");
    environments.put(MvnLauncher.ENV_NAME_MAVEN_OPTS, "-Xmx64m");
    final var workDirPath = StdFsPath.of(".");
    MvnLauncher.launch(workDirPath, environments, Arrays.asList("-version"));
  }

}
