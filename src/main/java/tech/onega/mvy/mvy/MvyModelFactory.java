package tech.onega.mvy.mvy;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.yaml.snakeyaml.DumperOptions;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import tech.onega.mvy.utils.Check;
import tech.onega.mvy.utils.Nullable;
import tech.onega.mvy.utils.StringUtils;

public class MvyModelFactory {

  @Nullable
  public static MvyModel.Artifact createArtifact(final String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    final var delimPositions = new ArrayList<Integer>();
    for (int position = 0; position < value.length(); position++) {
      if (value.charAt(position) == ':') {
        delimPositions.add(position);
      }
    }
    final String groupId;
    final String artifactId;
    final String version;
    if (delimPositions.size() < 1) {
      groupId = value;
      artifactId = null;
      version = null;
    }
    else if (delimPositions.size() == 1) {
      groupId = value.substring(0, delimPositions.get(0));
      artifactId = value.substring(delimPositions.get(0) + 1);
      version = null;
    }
    else {
      groupId = value.substring(0, delimPositions.get(0));
      artifactId = value.substring(delimPositions.get(0) + 1, delimPositions.get(1));
      version = value.substring(delimPositions.get(1) + 1);
    }
    final var artifact = new MvyModel.Artifact(groupId, artifactId, version);
    Check.valid(artifact);
    return artifact;
  }

  public static String createExecutionId(final MvyModel.Plugin plugin, final MvyModel.PluginExecution execution) {
    final var builder = new StringBuilder();
    builder.append(plugin.plugin().artifactId());
    if (StringUtils.isNotBlank(execution.phase())) {
      builder.append("-").append(execution.phase());
    }
    return builder.toString();
  }

  public static MvyModel.Project createProjectFromYaml(final String yaml) {
    try {
      final var options = new DumperOptions();
      final var yamlFactory = YAMLFactory.builder().dumperOptions(options).build();
      final var yamlMapper = new ObjectMapper(yamlFactory)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, true)
        .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
        .configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false)
        .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false)
        .setSerializationInclusion(Include.ALWAYS)
        .setDefaultPropertyInclusion(Include.ALWAYS)
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());
      final var project = yamlMapper.readValue(yaml, MvyModel.Project.class);
      Check.valid(project);
      return project;
    }
    catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public static MvyModel.Project createProjectFromYaml(final URI yamlEndpoint) {
    try (final var yamlStream = yamlEndpoint.toURL().openStream()) {
      final var yamlBytes = yamlStream.readAllBytes();
      return createProjectFromYaml(new String(yamlBytes, StandardCharsets.UTF_8));
    }
    catch (final Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
