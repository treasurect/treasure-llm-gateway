package com.treasure.llmserver.controller;

import com.treasure.llm.core.LlmClient;
import com.treasure.llm.core.model.LlmRequest;
import com.treasure.llm.core.model.LlmResponse;
import com.treasure.llm.core.model.LlmStreamEvent;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LlmClient llmClient;

    public LlmController(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @PostMapping("/chat")
    public LlmResponse chat(@RequestBody LlmRequest request) {
        return llmClient.chat(request);
    }

    @PostMapping("/chat/async")
    public Mono<LlmResponse> chatAsync(@RequestBody LlmRequest request) {
        return llmClient.chatAsync(request);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<LlmStreamEvent>> chatStream(@RequestBody LlmRequest request) {
        return llmClient.chatStream(request)
                .map(event -> ServerSentEvent.builder(event).build());
    }
}
