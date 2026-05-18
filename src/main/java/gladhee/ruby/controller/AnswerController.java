// Replace package with your project's base package if needed
package gladhee.ruby.controller;

import app.rag.dto.AnswerDtos;
import app.rag.service.AnswerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/answer")
public class AnswerController {
    private final AnswerService answerService;
    public AnswerController(AnswerService answerService) { this.answerService = answerService; }

    @PostMapping
    public AnswerDtos.AnswerResponse answer(@RequestBody AnswerDtos.AnswerRequest req) {
        if (req == null || req.sessionId() == null || req.sessionId().isBlank() ||
            req.query() == null || req.query().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId and query are required");
        }
        return answerService.answer(req);
    }
}
