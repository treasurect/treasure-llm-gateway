package com.treasure.llmserver.service;

import com.treasure.common.UserContext;
import com.treasure.llm.core.exception.LlmException;
import com.treasure.llm.core.model.LlmUsage;
import com.treasure.llmserver.mapper.LlmCallLogMapper;
import com.treasure.llmserver.record.LlmCallLog;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmCallLogRecorderTest {

    private RecordingMapper mapper;

    private LlmCallLogRecorder recorder;

    @BeforeEach
    void setUp() {
        mapper = new RecordingMapper();
        recorder = new LlmCallLogRecorder(mapper);
        UserContext.setUserId(42L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldRecordSuccessUsage() {
        long startTime = System.currentTimeMillis() - 100;

        recorder.recordSuccess(
                "deepseek-flash",
                new LlmUsage(10, 20, 30),
                "stop",
                startTime,
                false
        );

        LlmCallLog log = mapper.lastLog;
        assertThat(log.getUserId()).isEqualTo(42L);
        assertThat(log.getModelId()).isEqualTo("deepseek-flash");
        assertThat(log.getStream()).isFalse();
        assertThat(log.getPromptTokens()).isEqualTo(10);
        assertThat(log.getCompletionTokens()).isEqualTo(20);
        assertThat(log.getTotalTokens()).isEqualTo(30);
        assertThat(log.getFinishReason()).isEqualTo("stop");
        assertThat(log.getStatus()).isEqualTo("SUCCESS");
        assertThat(log.getLatencyMs()).isGreaterThanOrEqualTo(100);
    }

    @Test
    void shouldRecordFailure() {
        long startTime = System.currentTimeMillis() - 50;

        recorder.recordFailure(
                "qwen-flash",
                startTime,
                true,
                new LlmException("MODEL_NOT_FOUND", "Model not found: qwen-flash")
        );

        LlmCallLog log = mapper.lastLog;
        assertThat(log.getModelId()).isEqualTo("qwen-flash");
        assertThat(log.getStream()).isTrue();
        assertThat(log.getStatus()).isEqualTo("FAILED");
        assertThat(log.getErrorCode()).isEqualTo("MODEL_NOT_FOUND");
        assertThat(log.getErrorMessage()).isEqualTo("Model not found: qwen-flash");
    }

    private static class RecordingMapper implements LlmCallLogMapper {

        private LlmCallLog lastLog;

        @Override
        public int insert(LlmCallLog log) {
            this.lastLog = log;
            return 1;
        }
    }
}
