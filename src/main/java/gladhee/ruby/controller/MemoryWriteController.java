// Replace package with your project's base package if needed
package gladhee.ruby.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/v1/memory")
public class MemoryWriteController {
    private final VectorStore vectorStore;
    public MemoryWriteController(VectorStore vectorStore) { this.vectorStore = vectorStore; }

    public record WriteRequest(String sessionId, String role, String text, Map<String,Object> extra) {}
    public record WriteResponse(String id) {}
    public record BulkWriteRequest(String sessionId, List<WriteRequest> items) {}
    public record BulkWriteResponse(List<String> ids) {}

    @PostMapping("/write")
    public WriteResponse write(@RequestBody WriteRequest req) {
        if (req == null || req.sessionId() == null || req.sessionId().isBlank() ||
            req.text() == null || req.text().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId and text are required");
        }
        String id = UUID.randomUUID().toString();
        Map<String, Object> md = new HashMap<>();
        md.put("bucket", "memory");
        md.put("session_id", req.sessionId());
        md.put("role", req.role()==null? "user" : req.role());
        if (req.extra()!=null) md.putAll(req.extra());
        Document d = new Document(id, req.text(), md);
        vectorStore.add(List.of(d));
        return new WriteResponse(id);
    }

    @PostMapping("/bulk")
    public BulkWriteResponse bulk(@RequestBody BulkWriteRequest req) {
        if (req == null || req.sessionId()==null || req.sessionId().isBlank() || req.items()==null || req.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId and items are required");
        }
        List<Document> docs = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (WriteRequest it : req.items()) {
            if (it.text()==null || it.text().isBlank()) continue;
            String id = UUID.randomUUID().toString();
            Map<String, Object> md = new HashMap<>();
            md.put("bucket","memory");
            md.put("session_id", req.sessionId());
            md.put("role", it.role()==null? "user":it.role());
            if (it.extra()!=null) md.putAll(it.extra());
            docs.add(new Document(id, it.text(), md));
            ids.add(id);
        }
        if (!docs.isEmpty()) vectorStore.add(docs);
        return new BulkWriteResponse(ids);
    }
}
