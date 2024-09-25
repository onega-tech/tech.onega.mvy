package tech.onega.mvy.mvn;

import java.util.LinkedHashSet;
import java.util.List;
import tech.onega.mvy.mvy.MvyModel;
import tech.onega.mvy.mvy.MvyModelFactory;
import tech.onega.mvy.utils.Nullable;
import tech.onega.mvy.utils.StringUtils;

public class MvnPomXmlFactory {

  private final MvyModel.Project project;

  private final XmlBuilder xmlBuilder;

  public MvnPomXmlFactory(final MvyModel.Project project) {
    this.project = project;
    this.xmlBuilder = new XmlBuilder();
  }

  public String buildPomXml() {
    this.xmlBuilder.clear();
    this.writeProject();
    return this.xmlBuilder.toString();
  }

  private void writeBuild() {
    if (this.project.build() == null) {
      return;
    }
    this.xmlBuilder.appendLine();
    this.xmlBuilder.appendTagOpen(2, "build");
    this.writeBuildPlugins();
    this.xmlBuilder.appendTagClose(2, "build");
  }

  private void writeBuildPlugins() {
    if (this.project.build().plugins() == null || this.project.build().plugins().isEmpty()) {
      return;
    }
    this.xmlBuilder.appendTagOpen(4, "plugins");
    for (final var plugin : this.project.build().plugins()) {
      this.xmlBuilder.appendTagOpen(6, "plugin");
      this.xmlBuilder
        .appendTagIfNotBlank(8, "groupId", plugin.plugin().groupId())
        .appendTagIfNotBlank(8, "artifactId", plugin.plugin().artifactId())
        .appendTagIfNotBlank(8, "version", plugin.plugin().version())
        .appendTagIfNotBlank(8, "inherited", plugin.inherited());
      //dependencies
      if (plugin.dependencies() != null && !plugin.dependencies().isEmpty()) {
        this.xmlBuilder.appendTagOpen(8, "dependencies");
        for (final var dependency : plugin.dependencies()) {
          this.writeDependency(10, dependency, null, null);
        }
        this.xmlBuilder.appendTagClose(8, "dependencies");
      }
      //configuration
      if (plugin.configuration() != null && !plugin.configuration().isEmpty()) {
        this.xmlBuilder.appendTagOpen(8, "configuration");
        final var propTree = YamlConfiguration.createFromMap(plugin.configuration());
        this.xmlBuilder.append(propTree.toXml(10)).append("\n");
        this.xmlBuilder.appendTagClose(8, "configuration");
      }
      //executions
      if (plugin.executions() != null && !plugin.executions().isEmpty()) {
        this.xmlBuilder.appendTagOpen(8, "executions");
        for (final var execution : plugin.executions()) {
          this.xmlBuilder.appendTagOpen(10, "execution");
          final var executionId = StringUtils.isNotBlank(execution.id())
            ? execution.id()
            : MvyModelFactory.createExecutionId(plugin, execution);
          this.xmlBuilder
            .appendTagIfNotBlank(12, "id", executionId)
            .appendTagIfNotBlank(12, "phase", execution.phase());
          if (StringUtils.isNotBlank(execution.goals())) {
            final var goals = execution.goals().replaceAll("[ ]?", "").split("\\,");
            if (goals.length > 0) {
              this.xmlBuilder.appendTagOpen(12, "goals");
              for (final var goal : goals) {
                this.xmlBuilder.appendTagIfNotBlank(14, "goal", goal);
              }
              this.xmlBuilder.appendTagClose(12, "goals");
            }
          }
          if (execution.configuration() != null && !execution.configuration().isEmpty()) {
            this.xmlBuilder.appendTagOpen(12, "configuration");
            final var propTree = YamlConfiguration.createFromMap(execution.configuration());
            this.xmlBuilder.append(propTree.toXml(12)).append("\n");
            this.xmlBuilder.appendTagClose(12, "configuration");
          }
          this.xmlBuilder.appendTagClose(10, "execution");
        }
        this.xmlBuilder.appendTagClose(8, "executions");
      }
      this.xmlBuilder.appendTagClose(6, "plugin");
    }
    this.xmlBuilder.appendTagClose(4, "plugins");
  }

  private void writeDependencies() {
    if (this.project.dependencies() == null) {
      return;
    }
    this.xmlBuilder.appendLine();
    this.xmlBuilder.appendTagOpen(2, "dependencies");
    final var commonExclusions = this.project.dependencies().exclusions()
      .stream()
      .map(MvyModel.Dependency::artifact)
      .toList();
    this.writeDependenciesByScope(this.project.dependencies().compile(), "compile", commonExclusions);
    this.writeDependenciesByScope(this.project.dependencies().test(), "test", commonExclusions);
    this.writeDependenciesByScope(this.project.dependencies().provided(), "provided", commonExclusions);
    this.writeDependenciesByScope(this.project.dependencies().runtime(), "runtime", commonExclusions);
    this.writeDependenciesByScope(this.project.dependencies().system(), "system", commonExclusions);
    this.xmlBuilder.appendTagClose(2, "dependencies");
  }

  private void writeDependenciesByScope(final List<MvyModel.Dependency> dependencies, final String scope, @Nullable final List<MvyModel.Artifact> commonExclusions) {
    if (dependencies != null && !dependencies.isEmpty()) {
      for (final var dependency : dependencies) {
        this.writeDependency(4, dependency, scope, commonExclusions);
      }
    }
  }

  private void writeDependency(final int indent, final MvyModel.Dependency dependency, @Nullable final String scope, @Nullable final List<MvyModel.Artifact> commonExclusions) {
    final var dependencyExclusions = new LinkedHashSet<MvyModel.Artifact>();
    if (dependency.exclusions() != null) {
      dependencyExclusions.addAll(dependency.exclusions());
    }
    if (commonExclusions != null) {
      dependencyExclusions.addAll(commonExclusions);
    }
    //
    this.xmlBuilder.appendTagOpen(indent, "dependency");
    this.xmlBuilder
      .appendTagIfNotBlank(indent + 2, "groupId", dependency.artifact().groupId())
      .appendTagIfNotBlank(indent + 2, "artifactId", dependency.artifact().artifactId())
      .appendTagIfNotBlank(indent + 2, "version", dependency.artifact().version())
      .appendTagIfNotBlank(indent + 2, "type", dependency.type())
      .appendTagIfNotBlank(indent + 2, "scope", scope)
      .appendTagIfNotBlank(indent + 2, "systemPath", dependency.systemPath());
    if (!dependencyExclusions.isEmpty()) {
      this.xmlBuilder.appendTagOpen(indent + 2, "exclusions");
      for (final var dependencyExclusion : dependencyExclusions) {
        this.xmlBuilder
          .appendTagOpen(indent + 4, "exclusion")
          .appendTagIfNotBlank(indent + 6, "groupId", dependencyExclusion.groupId())
          .appendTagIfNotBlank(indent + 6, "artifactId", dependencyExclusion.artifactId())
          .appendTagClose(indent + 4, "exclusion");
      }
      this.xmlBuilder.appendTagClose(indent + 2, "exclusions");
    }
    this.xmlBuilder.appendTagClose(indent, "dependency");
  }

  private void writeProject() {
    this.xmlBuilder
      .appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
      .appendLine("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"")
      .appendLine("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
      .appendLine("  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">")
      .appendLine()
      .appendTag(2, "modelVersion", "4.0.0")
      .appendTagIfNotBlank(2, "packaging", this.project.packaging());
    if (this.project.artifact() != null) {
      this.xmlBuilder
        .appendLine()
        .appendTagIfNotBlank(2, "groupId", this.project.artifact().groupId())
        .appendTagIfNotBlank(2, "artifactId", this.project.artifact().artifactId())
        .appendTagIfNotBlank(2, "version", this.project.artifact().version());
    }
    this.writeProperties();
    this.writeRepositories();
    this.writeDependencies();
    this.writeBuild();
    this.xmlBuilder
      .appendLine()
      .appendTagClose(0, "project");
  }

  private void writeProperties() {
    if (this.project.properties() == null || this.project.properties().isEmpty()) {
      return;
    }
    this.xmlBuilder.appendLine();
    this.xmlBuilder.appendTagOpen(2, "properties");
    for (final var kv : this.project.properties().entrySet()) {
      this.xmlBuilder.appendTagIfNotBlank(4, kv.getKey(), kv.getValue());
    }
    this.xmlBuilder.appendTagClose(2, "properties");
  }

  private void writeRepositories() {
    if (this.project.repositories() == null || this.project.repositories().isEmpty()) {
      return;
    }
    this.xmlBuilder.appendLine();
    this.xmlBuilder.appendTagOpen(2, "repositories");
    for (final var kv : this.project.repositories().entrySet()) {
      final var repositoryId = kv.getKey();
      final var repository = kv.getValue();
      this.xmlBuilder
        .appendTagOpen(4, "repository")
        .appendTag(6, "id", repositoryId)
        .appendTagIfNotBlank(6, "url", repository.url())
        .appendTagIfNotBlank(6, "name", repository.name())
        .appendTagIfNotBlank(6, "layout", repository.layout());
      if (StringUtils.isAnyValueIsNotBlank(repository.releasesEnabled(), repository.releasesChecksumPolicy(), repository.releasesUpdatePolicy())) {
        this.xmlBuilder
          .appendTagOpen(6, "releases")
          .appendTagIfNotBlank(8, "enabled", repository.releasesEnabled())
          .appendTagIfNotBlank(8, "checksumPolicy", repository.releasesChecksumPolicy())
          .appendTagIfNotBlank(8, "updatePolicy", repository.releasesUpdatePolicy())
          .appendTagClose(6, "releases");
      }
      if (StringUtils.isAnyValueIsNotBlank(repository.snapshotsEnabled(), repository.snapshotsChecksumPolicy(), repository.snapshotsUpdatePolicy())) {
        this.xmlBuilder
          .appendTagOpen(6, "snapshots")
          .appendTagIfNotBlank(8, "enabled", repository.snapshotsEnabled())
          .appendTagIfNotBlank(8, "checksumPolicy", repository.snapshotsChecksumPolicy())
          .appendTagIfNotBlank(8, "updatePolicy", repository.snapshotsUpdatePolicy())
          .appendTagClose(6, "snapshots");
      }
      this.xmlBuilder.appendTagClose(4, "repository");
    }
    this.xmlBuilder.appendTagClose(2, "repositories");
  }

}
