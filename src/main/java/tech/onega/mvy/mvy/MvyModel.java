package tech.onega.mvy.mvy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tech.onega.mvy.utils.Nullable;

public class MvyModel {

  public record Artifact(
    @NotBlank String groupId,
    @NotBlank String artifactId,
    @Nullable String version) {

    @JsonCreator
    public static Artifact create(final String value) {
      return MvyModelFactory.createArtifact(value);
    }

  }

  public record Dependency(
    @NotNull @Valid MvyModel.Artifact artifact,
    @Nullable String type,
    @Nullable String systemPath,
    @Nullable @Valid List<MvyModel.Artifact> exclusions) {
  }

  public record Dependencies(
    @Nullable @Valid List<MvyModel.Dependency> compile,
    @Nullable @Valid List<MvyModel.Dependency> provided,
    @Nullable @Valid List<MvyModel.Dependency> runtime,
    @Nullable @Valid List<MvyModel.Dependency> test,
    @Nullable @Valid List<MvyModel.Dependency> system,
    @Nullable @Valid List<MvyModel.Dependency> exclusions) {
  }

  public record PluginExecution(
    @Nullable String id,
    @Nullable String phase,
    @Nullable String goals,
    @Nullable LinkedHashMap<String, Object> configuration,
    @Nullable String inherited) {
  }

  public record Plugin(
    @NotNull @Valid MvyModel.Artifact plugin,
    @Nullable Map<String, Object> configuration,
    @Nullable String scope,
    @Nullable String inherited,
    @Nullable @Valid List<MvyModel.PluginExecution> executions,
    @Nullable @Valid List<MvyModel.Dependency> dependencies) {
  }

  public record Repository(
    @NotBlank String url,
    @Nullable String name,
    @Nullable String layout,
    @Nullable String releasesEnabled,
    @Nullable String releasesUpdatePolicy,
    @Nullable String releasesChecksumPolicy,
    @Nullable String snapshotsEnabled,
    @Nullable String snapshotsUpdatePolicy,
    @Nullable String snapshotsChecksumPolicy) {
  }

  public record Build(
    @Nullable @Valid List<MvyModel.Plugin> plugins) {
  }

  public record Project(
    @NotBlank String mvy,
    @NotNull @Valid MvyModel.Artifact artifact,
    @NotBlank String java,
    @NotBlank String packaging,
    @Nullable LinkedHashMap<String, String> properties,
    @Nullable @Valid MvyModel.Dependencies dependencies,
    @Nullable @Valid LinkedHashMap<String, MvyModel.Repository> repositories,
    @Nullable @Valid MvyModel.Build build) {
  }

}
