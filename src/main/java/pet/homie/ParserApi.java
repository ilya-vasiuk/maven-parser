package pet.homie;

import org.json.JSONArray;
import org.json.JSONObject;
import pet.homie.model.Artifact;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/parse")
public class ParserApi {

    @GET
    @Path("{groupId}/{artifactId}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public String parse(@PathParam("groupId") String groupId,
                        @PathParam("artifactId") String artifactId,
                        @PathParam("version") String version) {

        Artifact root = new Artifact(groupId, artifactId, version);
        PomParser parser = new PomParser();

        return packArtifact(parser.parse(root)).toString();
    }

    private static JSONObject packArtifact(Artifact artifact) {
        JSONObject json = new JSONObject();

        json.put("groupId", artifact.getGroupId());
        json.put("artifactId", artifact.getArtifactId());
        json.put("version", artifact.getVersion());
        json.put("dependencies", packDependencies(artifact.getDependencies()));

        return json;
    }

    private static JSONArray packDependencies(List<Artifact> dependencies) {
        JSONArray json = new JSONArray();

        dependencies.stream()
                .map(ParserApi::packArtifact)
                .forEach(json::put);

        return json;
    }
}
