package gladhee.ruby.controller;

import gladhee.ruby.dto.ResultDtos;
import gladhee.ruby.service.EsLoggingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ResultController {
	private final EsLoggingService logs;

	@PostMapping("/result")
	public ResultDtos.SaveResultResponse save(@Valid @RequestBody ResultDtos.SaveResultRequest req) throws Exception {
		return new ResultDtos.SaveResultResponse(logs.save(req));
	}
}
