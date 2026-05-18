package gladhee.ruby;

import gladhee.ruby.service.RetrievalService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RetrievalServiceTest {
    @Test
    void filters_by_minScore() {
        VectorStore vs = Mockito.mock(VectorStore.class);
        Document d1 = new Document("a", "hello", Map.of("ai_similarity_score", 0.9));
        Document d2 = new Document("b", "world", Map.of("ai_similarity_score", 0.5));
        when(vs.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(d1, d2));

        RetrievalService svc = new RetrievalService(vs);
        var got = svc.retrieve("s1", "q", 10, 0.8);
        assertEquals(1, got.size());
        assertEquals("a", got.get(0).doc().getId());
    }
}
