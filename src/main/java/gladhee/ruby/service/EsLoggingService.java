// Replace package with your project's base package if needed
package gladhee.ruby.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

@Service
public class EsLoggingService {
    private final WebClient http;

    public EsLoggingService(@Value("${spring.elasticsearch.uris:http://localhost:9200}") String esUri) {
        this.http = WebClient.builder().baseUrl(esUri).build();
    }

    public String saveMap(Map<String, Object> doc) {
        String id = UUID.randomUUID().toString();
        try {
            http.put()
                    .uri("/logs/_doc/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(doc)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return id;
        } catch (Exception e) {
            return "failed:" + id;
        }
    }
}
