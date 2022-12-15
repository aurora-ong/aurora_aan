package ong.aurora.commons.projector.ksaprojector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ong.aurora.commons.command.CommandProjectorQueryException;
import ong.aurora.commons.entity.AANEntity;
import ong.aurora.commons.entity.EntityValue;
import ong.aurora.commons.entity.MaterializedEntity;
import ong.aurora.commons.projector.AANProjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class KSAProjector implements AANProjector {

    private static final Logger logger = LoggerFactory.getLogger(KSAProjector.class);

    String host;

    HttpClient httpClient;

    public KSAProjector(String host) {
        this.host = host;
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public <K, V extends EntityValue<V>> List<MaterializedEntity<V>> queryAll(AANEntity<K, V> entity) throws CommandProjectorQueryException {
        try {
            String entityAllPath = this.host.concat("/").concat(entity.entityName).concat("/all");
            logger.info("Consultando ruta {}", entityAllPath);
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            builder.uri(new URI(entityAllPath));
            builder.GET();
            HttpRequest request = builder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, TypeFactory.defaultInstance().constructParametricType(MaterializedEntity.class, entity.valueType));

            List<MaterializedEntity<V>> list = KSAProjector.fromJSON(response.body(), type);

            logger.info("Respuesta {}", list.toString());

            return list;

        } catch (Exception e) {
            logger.error("Error consulta", e);
            throw new CommandProjectorQueryException(e.toString());
        }

    }

    @Override
    public <K, V extends EntityValue<V>> Optional<MaterializedEntity<V>> queryOne(AANEntity<K, V> entity, K id) throws CommandProjectorQueryException {
        try {
            String entityOnePath = this.host.concat("/").concat(entity.entityName).concat("/one");
            logger.info("Consultando ruta {}", entityOnePath);

            HttpRequest.Builder builder = HttpRequest.newBuilder();
            builder.uri(new URI(entityOnePath));
            builder.POST(HttpRequest.BodyPublishers.ofString(KSAProjector.toJSON(id), StandardCharsets.UTF_8));
            HttpRequest request = builder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 404) {
                logger.info("No encontrado");
                return Optional.empty();
            }

            JavaType type = TypeFactory.defaultInstance().constructParametricType(MaterializedEntity.class, TypeFactory.defaultInstance().constructType(entity.valueType));

            MaterializedEntity<V> element = KSAProjector.fromJSON(response.body(), type);

            logger.info("Respuesta {}", element.toString());

            return Optional.of(element);

        } catch (Exception e) {
            logger.error("Error consulta", e);
            throw new CommandProjectorQueryException(e.toString());
        }
    }

    static private <T> T fromJSON(String json, JavaType typeReference) {
        ObjectMapper objectMapper =
                new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static private String toJSON(Object o) {
        ObjectMapper objectMapper =
                new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
