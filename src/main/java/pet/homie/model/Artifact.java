package pet.homie.model;

import org.apache.maven.model.Dependency;

import java.util.ArrayList;
import java.util.List;

public class Artifact {
    private final Dependency artifact;
    private final List<Artifact> dependencies = new ArrayList<>();

    public Artifact(String groupId, String artifactId, String version) {
        this.artifact = buildArtifact(groupId, artifactId, version);
    }

    public String getGroupId() {
        return artifact.getGroupId();
    }

    public String getArtifactId() {
        return artifact.getArtifactId();
    }

    public String getVersion() {
        return artifact.getVersion();
    }

    public List<Artifact> getDependencies() {
        return dependencies;
    }

    private Dependency buildArtifact(String groupId, String artifactId, String version) {
        Dependency dependency = new Dependency();

        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);

        return dependency;
    }
}
