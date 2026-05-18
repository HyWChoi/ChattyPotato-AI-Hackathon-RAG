package gladhee.ruby.controller;

import gladhee.ruby.dto.MemoryDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping(path = "/v1/memory", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MemoryController {

	private final VectorStore vectorStore; // ElasticsearchVectorStore 자동 주입 (index=memory)

	// 매 턴 종료 시, user/assistant 각각 핵심 요약·사실 위주로 write 권장
	@PostMapping("/write")
	public Map<String,Object> write(@Valid @RequestBody MemoryDtos.WriteRequest req) {
		Map<String,Object> meta = new HashMap<>();
		meta.put("bucket", "memory");
		meta.put("session_id", req.sessionId());
		meta.put("role", req.role());
		meta.put("ts", Instant.now().toString());
		if (req.extra()!=null) meta.putAll(req.extra());

		var doc = Document.builder()
				.id(UUID.randomUUID().toString())
				.text(req.text())
				.metadata(meta)
				.build();
		vectorStore.add(List.of(doc)); // 내부에서 임베딩 → ES 색인
		return Map.of("ok", true);
	}

	// 현재 질의와 유사한 메모리 topK 조회 (세션 범위 필터)
	@PostMapping("/search")
	public MemoryDtos.SearchResponse search(@Valid @RequestBody MemoryDtos.SearchRequest req) {
		int k = Optional.ofNullable(req.topK()).orElse(5);
		String filter = "bucket == 'memory' && session_id == '" + req.sessionId() + "'";
		var hits = vectorStore.similaritySearch(
				org.springframework.ai.vectorstore.SearchRequest.builder()
						.query(req.query())
						.topK(k)
						.filterExpression(filter)
						.build()
		);
		var items = hits.stream().map(d -> new MemoryDtos.Item(d.getId(), d.getScore(), d.getText(), d.getMetadata())).toList();
		return new MemoryDtos.SearchResponse(items);
	}
}
