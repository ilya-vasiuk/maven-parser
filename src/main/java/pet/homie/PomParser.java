package pet.homie;

import com.google.common.base.CaseFormat;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pet.homie.model.Artifact;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

public class PomParser {

    private static final Logger LOG = LoggerFactory.getLogger(PomParser.class);
    private static final String URL_PATTERN = "http://central.maven.org/maven2/%s/%s/%s/%s-%s.pom";

    public Artifact parse(Artifact root) {
        LOG.info(root.getGroupId() + ":" + root.getArtifactId() + ":" + root.getVersion());

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;

        try {
            InputStream is = getPomStream(root);
            if(is != null) {
                try {
                    model = reader.read(is);
                } catch (Exception e) {
                    LOG.info("POM wasn't parsed correctly: " + e);
                }

                if (model != null) {
                    root.getDependencies().addAll(parseDependencies(model.getDependencies(), model.getProperties()));
                    if (model.getDependencyManagement() != null) {
                        root.getDependencies().addAll(parseDependencies(model.getDependencyManagement().getDependencies(),
                                model.getProperties()));
                    }
                }

                is.close();
            } else {
                LOG.info("Didn't find pom file");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }                LOG.info("Camelized artifact Id");


        return root;
    }

    private List<Artifact> parseDependencies(List<Dependency> dependencies, Properties properties) {
        List<Artifact> parsed = new ArrayList<>();

        if (dependencies != null) {
            for (Dependency dependency : dependencies) {
                Artifact artifact = buildArtifact(dependency, properties);

                if (artifact.getGroupId() != null &&
                        artifact.getArtifactId() != null &&
                        artifact.getVersion() != null) {
                    artifact = parse(artifact);
                }

                parsed.add(artifact);
            }
        }

        return parsed;
    }

    private static Artifact buildArtifact(Dependency dependency, Properties properties) {
        Function<String, String> getProperty = (key) ->
                key == null ? key : properties.getProperty(unwrapPropertyName(key), key);

        return new Artifact(
                getProperty.apply(dependency.getGroupId()),
                getProperty.apply(dependency.getArtifactId()),
                getProperty.apply(dependency.getVersion())
        );
    }

    private static String unwrapPropertyName(String name) {
        return name.replaceAll("\\$\\{(.+)\\}", "$1");
    }

    private InputStream getPomStream(Artifact artifact) {
        InputStream is;
        Function<String, String> camelize = (id) -> CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, id);

        try {
            is = buildPomURL(artifact, (id) -> id).openStream();
        } catch (IOException e) {
            try {
                is = buildPomURL(artifact, camelize).openStream();
                LOG.info("Camelized artifact Id");
            } catch (IOException e1) {
                return null;
            }
        }

        return is;
    }

    private static URL buildPomURL(Artifact artifact,
                                            Function<String, String> artifactIdModifier) {
        try {
            return new URL(String.format(URL_PATTERN,
                    artifact.getGroupId().replaceAll("\\.", "/"),
                    artifactIdModifier.apply(artifact.getArtifactId()),
                    artifact.getVersion(),
                    artifactIdModifier.apply(artifact.getArtifactId()),
                    artifact.getVersion()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
