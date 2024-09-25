package tech.onega.mvy.app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.testng.annotations.Test;
import tech.onega.mvy.utils.Check;

class AppServiceTest {

  @Test
  void testCreatePomXml() throws IOException {
    try (var appService = new AppService()) {
      final var yamlFile = new File("src/test/resources/v0.1/mvy.yaml");
      final var pomXml = appService.createPomXml(yamlFile.toURI());
      final var expectedPomXmlFile = new File("src/test/resources/v0.1/pom.xml");
      //Files.write(expectedPomXmlFile.toPath(), pomXml.getBytes());
      final var expectedPom = new String(Files.readAllBytes(expectedPomXmlFile.toPath()), StandardCharsets.UTF_8);
      Check.equals(pomXml, expectedPom);
    }
  }

}