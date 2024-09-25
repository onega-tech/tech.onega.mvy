package tech.onega.mvy.mvn;

import java.util.Arrays;
import java.util.LinkedHashMap;
import org.testng.annotations.Test;
import tech.onega.mvy.utils.Check;

class YamlConfigurationTest {

  @Test
  void testKey() {
    {
      final var key = new YamlConfiguration.Key("project");
      Check.equals(key.name, "project");
      Check.equals(key.tag, "project");
      Check.equals(key.attr, null);
      Check.equals(key.index, null);
      Check.equals(key.tagChain, Arrays.asList("project"));
    }
    {
      final var key = new YamlConfiguration.Key("project.modelVersion");
      Check.equals(key.name, "project.modelVersion");
      Check.equals(key.tag, "modelVersion");
      Check.equals(key.attr, null);
      Check.equals(key.index, null);
      Check.equals(key.tagChain, Arrays.asList("project", "modelVersion"));
    }
    {
      final var key = new YamlConfiguration.Key("project.dependencies.dependency[0]");
      Check.equals(key.name, "project.dependencies.dependency[0]");
      Check.equals(key.tag, "dependency");
      Check.equals(key.attr, null);
      Check.equals(key.index, "0");
      Check.equals(key.tagChain, Arrays.asList("project", "dependencies", "dependency[0]"));
    }
    {
      final var key = new YamlConfiguration.Key("project.dependencies.dependency[1]@attrNameA");
      Check.equals(key.name, "project.dependencies.dependency[1]@attrNameA");
      Check.equals(key.tag, "dependency");
      Check.equals(key.attr, "attrNameA");
      Check.equals(key.index, "1");
      Check.equals(key.tagChain, Arrays.asList("project", "dependencies", "dependency[1]"));
    }
    Check.withThrow(() -> new YamlConfiguration.Key(null));
    Check.withThrow(() -> new YamlConfiguration.Key(""));
    Check.withThrow(() -> new YamlConfiguration.Key("@"));
    Check.withThrow(() -> new YamlConfiguration.Key("["));
    Check.withThrow(() -> new YamlConfiguration.Key("]"));
    Check.withThrow(() -> new YamlConfiguration.Key("[]"));
    Check.withThrow(() -> new YamlConfiguration.Key("a[]"));
    Check.withThrow(() -> new YamlConfiguration.Key("a[]@"));
    Check.withThrow(() -> new YamlConfiguration.Key("a@a[1]"));
    Check.withThrow(() -> new YamlConfiguration.Key("a[1][2]"));
    Check.withThrow(() -> new YamlConfiguration.Key("a@a.d"));
  }

  @Test
  void testToXmlA() {
    final LinkedHashMap<String, Object> propMap;
    {
      propMap = new LinkedHashMap<String, Object>();
      propMap.put("project.modelVersion", "4.0.0");
      propMap.put("project.groupId", "tech.onega");
      propMap.put("project.artifactId", "jnb");
      propMap.put("project.packaging", "jar");
      propMap.put("project.enable", "");
      propMap.put("project.version", "version");
      propMap.put("project.dependencies.dependency[0].groupId", "org.slf4j");
      propMap.put("project.dependencies.dependency[0].artifactId", "slf4j-simple");
      propMap.put("project.dependencies.dependency[0].version", "1.7.36");
      propMap.put("project.dependencies.dependency[1]", "ddd");
      propMap.put("project.dependencies.dependency[1].groupId", "org.apache.bval");
      propMap.put("project.dependencies.dependency[1].artifactId", "bval-jsr");
      propMap.put("project.dependencies.dependency[1].version", "3.0.0");
      propMap.put("project.dependencies.dependency[1]@attrNameA", "attrValueA");
      propMap.put("project.dependencies.dependency[1]@attrNameB", "attrValueB");
      final var subPropMap = new LinkedHashMap<String, Object>();
      subPropMap.put("subK", "subV");
      propMap.put("sub", subPropMap);
      propMap.put("vals.list", Arrays.asList(1, 2, 3, 4));
      propMap.put("vals.array", new int[] { 1, 2, 3, 4 });
    }
    final var yamlCfg = YamlConfiguration.createFromMap(propMap);
    final var xml = yamlCfg.toXml(2);
    final var expected = ""
      + "  <project>\n"
      + "    <modelVersion>4.0.0</modelVersion>\n"
      + "    <groupId>tech.onega</groupId>\n"
      + "    <artifactId>jnb</artifactId>\n"
      + "    <packaging>jar</packaging>\n"
      + "    <enable/>\n"
      + "    <version>version</version>\n"
      + "    <dependencies>\n"
      + "      <dependency>\n"
      + "        <groupId>org.slf4j</groupId>\n"
      + "        <artifactId>slf4j-simple</artifactId>\n"
      + "        <version>1.7.36</version>\n"
      + "      </dependency>\n"
      + "      <dependency attrNameA=\"attrValueA\" attrNameB=\"attrValueB\">\n"
      + "        ddd\n"
      + "        <groupId>org.apache.bval</groupId>\n"
      + "        <artifactId>bval-jsr</artifactId>\n"
      + "        <version>3.0.0</version>\n"
      + "      </dependency>\n"
      + "    </dependencies>\n"
      + "  </project>\n"
      + "  <sub>\n"
      + "    <subK>subV</subK>\n"
      + "  </sub>\n"
      + "  <vals>\n"
      + "    <list>1</list>\n"
      + "    <list>2</list>\n"
      + "    <list>3</list>\n"
      + "    <list>4</list>\n"
      + "    <array>1</array>\n"
      + "    <array>2</array>\n"
      + "    <array>3</array>\n"
      + "    <array>4</array>\n"
      + "  </vals>";
    Check.equals(xml, expected);
  }

  @Test
  void testToXmlB() {
    final LinkedHashMap<String, Object> propMap;
    {
      propMap = new LinkedHashMap<String, Object>();
      propMap.put("createDependencyReducedPom", "false");
      propMap.put("outputFile", "${project.build.directory}/${project.artifactId}.jar");
      propMap.put("filters.filter[0].artifact", "*:*");
      propMap.put("filters.filter[0].excludes.exclude[0]", ".gitkeep");
      propMap.put("filters.filter[0].excludes.exclude[1]", "META-INF/versions/");
      propMap.put("filters.filter[0].excludes.exclude[2]", "module-info.*");
      propMap.put("filters.filter[0].excludes.exclude[3]", "META-INF/*.SF");
      propMap.put("filters.filter[0].excludes.exclude[4]", "META-INF/*.MF");
      propMap.put("filters.filter[0].excludes.exclude[5]", "META-INF/JAXB");
      propMap.put("filters.filter[0].excludes.exclude[6]", "META-INF/DEPENDENCIES");
      propMap.put("filters.filter[0].excludes.exclude[7]", "META-INF/LICENSE");
      propMap.put("filters.filter[0].excludes.exclude[8]", "META-INF/NOTICE");
      propMap.put("filters.filter[0].excludes.exclude[9]", "META-INF/*-LICENSE");
      propMap.put("filters.filter[0].excludes.exclude[10]", "META-INF/*-NOTICE");
      propMap.put("filters.filter[0].excludes.exclude[11]", "META-INF/*.DSA");
      propMap.put("transformers.transformer[0]@implementation", "org.apache.maven.plugins.shade.resource.ServicesResourceTransformer");
      propMap.put("transformers.transformer[1]@implementation", "org.apache.maven.plugins.shade.resource.ManifestResourceTransformer");
      propMap.put("transformers.transformer[1].manifestEntries.Main-Class", "tech.onega.mvy.app.AppMain");
    }
    final var yamlCfg = YamlConfiguration.createFromMap(propMap);
    final var xml = yamlCfg.toXml(0);
    final var expected = ""
      + "<createDependencyReducedPom>false</createDependencyReducedPom>\n"
      + "<outputFile>${project.build.directory}/${project.artifactId}.jar</outputFile>\n"
      + "<filters>\n"
      + "  <filter>\n"
      + "    <artifact>*:*</artifact>\n"
      + "    <excludes>\n"
      + "      <exclude>.gitkeep</exclude>\n"
      + "      <exclude>META-INF/versions/</exclude>\n"
      + "      <exclude>module-info.*</exclude>\n"
      + "      <exclude>META-INF/*.SF</exclude>\n"
      + "      <exclude>META-INF/*.MF</exclude>\n"
      + "      <exclude>META-INF/JAXB</exclude>\n"
      + "      <exclude>META-INF/DEPENDENCIES</exclude>\n"
      + "      <exclude>META-INF/LICENSE</exclude>\n"
      + "      <exclude>META-INF/NOTICE</exclude>\n"
      + "      <exclude>META-INF/*-LICENSE</exclude>\n"
      + "      <exclude>META-INF/*-NOTICE</exclude>\n"
      + "      <exclude>META-INF/*.DSA</exclude>\n"
      + "    </excludes>\n"
      + "  </filter>\n"
      + "</filters>\n"
      + "<transformers>\n"
      + "  <transformer implementation=\"org.apache.maven.plugins.shade.resource.ServicesResourceTransformer\"/>\n"
      + "  <transformer implementation=\"org.apache.maven.plugins.shade.resource.ManifestResourceTransformer\">\n"
      + "    <manifestEntries>\n"
      + "      <Main-Class>tech.onega.mvy.app.AppMain</Main-Class>\n"
      + "    </manifestEntries>\n"
      + "  </transformer>\n"
      + "</transformers>";
    Check.equals(xml, expected);
  }

}