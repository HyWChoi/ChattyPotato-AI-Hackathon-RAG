package gladhee.ruby;

import gladhee.ruby.dto.AnswerDtos;
import gladhee.ruby.service.AnswerService;
import gladhee.ruby.service.EsLoggingService;
import gladhee.ruby.service.RetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

class AnswerServiceTest {
    ChatClient chat = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
    RetrievalService retrieval = Mockito.mock(RetrievalService.class);
    EsLoggingService es = Mockito.mock(EsLoggingService.class);
    AnswerService svc;

    @BeforeEach
    void setUp() {
        svc = new AnswerService(chat, retrieval, es) {
            @Override
            @SuppressWarnings("unchecked")
            public AnswerDtos.AnswerResponse answer(AnswerDtos.AnswerRequest req) {
                // Pretend LLM JSON outputs to avoid actual parsing dependency.
                Mockito.when(chat.prompt().system(Mockito.anyString()).user(Mockito.anyString()).call().content())
                        .thenReturn("{"rewritten":"best query","confidence":0.88,"route_hint":"cheap"}")
                        .thenReturn("{"answer":"ok","citations":[{"id":"x","snippet":"s"}]}");
                var d = new Document("x", "context text", Map.of("ai_similarity_score", 0.9));
                Mockito.when(retrieval.retrieve(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.any()))
                        .thenReturn(List.of(new RetrievalService.Retrieved(d, 0.9)));
                Mockito.when(es.saveMap(Mockito.anyMap())).thenReturn("id");
                return super.answer(req);
            }
        };
    }

    @Test
    void pipeline_happy_path() {
        var req = new AnswerDtos.AnswerRequest("s1", "original q", 5, 3, 0.2, null, null);
        var res = svc.answer(req);
        assertEquals("best query", res.rewritten());
        assertEquals("cheap", res.route());
        assertEquals("ok", res.answer());
        assertFalse(res.citations().isEmpty());
    }
}
