package com.treasure.llmserver;

import com.treasure.llm.core.LlmClient;
import com.treasure.llm.core.model.LlmModelDefinition;
import com.treasure.llm.core.model.LlmRequest;
import com.treasure.llm.core.model.LlmResponse;
import com.treasure.llm.core.model.LlmStreamEvent;
import com.treasure.llm.core.model.LlmUsage;
import com.treasure.llmserver.service.LlmCallLogRecorder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/llm")
public class LlmGatewayController {

    private final LlmClient llmClient;

    private final LlmCallLogRecorder llmCallLogRecorder;

    public LlmGatewayController(
            LlmClient llmClient,
            LlmCallLogRecorder llmCallLogRecorder
    ) {
        this.llmClient = llmClient;
        this.llmCallLogRecorder = llmCallLogRecorder;
    }

    @GetMapping("/models")
    public List<LlmModelDefinition> models() {
        return llmClient.listModels();
    }

    @PostMapping("/chat")
    public LlmResponse chat(@RequestBody LlmRequest request) {
        long startTime = System.currentTimeMillis();
        LlmResponse response = llmClient.chat(request);
        llmCallLogRecorder.recordSuccess(request.modelId(), response, startTime, false);
        return response;
    }

    @PostMapping("/chat/async")
    public Mono<LlmResponse> chatAsync(@RequestBody LlmRequest request) {
        long startTime = System.currentTimeMillis();
        return llmClient.chatAsync(request)
                .doOnSuccess(response -> llmCallLogRecorder.recordSuccess(
                        request.modelId(),
                        response,
                        startTime,
                        false
                ))
                .doOnError(error -> llmCallLogRecorder.recordFailure(
                        request.modelId(),
                        startTime,
                        false,
                        error
                ));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<LlmStreamEvent>> chatStream(@RequestBody LlmRequest request) {
        long startTime = System.currentTimeMillis();
        AtomicReference<LlmUsage> usageRef = new AtomicReference<>();
        AtomicReference<String> finishReasonRef = new AtomicReference<>();

        return llmClient.chatStream(request)
                .doOnNext(event -> {
                    if (event.type() == LlmStreamEvent.LlmStreamEventType.USAGE) {
                        usageRef.set(event.usage());
                    }
                    if (event.type() == LlmStreamEvent.LlmStreamEventType.COMPLETE) {
                        finishReasonRef.set(event.finishReason());
                    }
                })
                .doOnComplete(() -> llmCallLogRecorder.recordSuccess(
                        request.modelId(),
                        usageRef.get(),
                        finishReasonRef.get(),
                        startTime,
                        true
                ))
                .doOnError(error -> llmCallLogRecorder.recordFailure(
                        request.modelId(),
                        startTime,
                        true,
                        error
                ))
                .map(event -> ServerSentEvent.builder(event).build());
    }

    @PostMapping("/chat/guest")
    public LlmResponse chatGuest(@RequestBody LlmRequest request) {
        return chat(request);
    }
}
