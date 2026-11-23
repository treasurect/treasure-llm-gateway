package com.treasure.llmserver;

import com.treasure.llm.core.LlmClient;
import com.treasure.llm.core.model.LlmModelDefinition;
import com.treasure.llm.core.model.LlmRequest;
import com.treasure.llm.core.model.LlmResponse;
import com.treasure.llm.core.model.LlmStreamEvent;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/llm")
public class LlmGatewayController {

    private final LlmClient llmClient;

    public LlmGatewayController(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @GetMapping("/models")
    public List<LlmModelDefinition> models() {
        return llmClient.listModels();
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

    @PostMapping("/chat/guest")
    public LlmResponse chatGuest(@RequestBody LlmRequest request) {
        return llmClient.chat(request);
    }
}
