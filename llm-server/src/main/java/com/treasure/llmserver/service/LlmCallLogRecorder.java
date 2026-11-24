package com.treasure.llmserver.service;

import com.treasure.llm.core.exception.LlmException;
import com.treasure.llm.core.model.LlmResponse;
import com.treasure.llm.core.model.LlmUsage;
import com.treasure.llmserver.mapper.LlmCallLogMapper;
import com.treasure.llmserver.record.LlmCallLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LlmCallLogRecorder {

    private static final Logger log = LoggerFactory.getLogger(LlmCallLogRecorder.class);

    private final LlmCallLogMapper llmCallLogMapper;

    public LlmCallLogRecorder(LlmCallLogMapper llmCallLogMapper) {
        this.llmCallLogMapper = llmCallLogMapper;
    }

    public void recordSuccess(String modelId, LlmResponse response, long startTime, boolean stream) {
        record(modelId, response.usage(), response.finishReason(), "SUCCESS", null, null, startTime, stream);
    }

    public void recordSuccess(String modelId, LlmUsage usage, String finishReason, long startTime, boolean stream) {
        record(
                modelId,
                usage,
                finishReason,
                "SUCCESS",
                null,
                null,
                startTime,
                stream
        );
    }

    public void recordFailure(String modelId, long startTime, boolean stream, Throwable error) {
        String errorCode = error instanceof LlmException llmException
                ? llmException.getErrorCode()
                : error.getClass().getSimpleName();

        record(
                modelId,
                null,
                null,
                "FAILED",
                errorCode,
                error.getMessage(),
                startTime,
                stream
        );
    }

    private void record(
            String modelId,
            LlmUsage usage,
            String finishReason,
            String status,
            String errorCode,
            String errorMessage,
            long startTime,
            boolean stream
    ) {
        LlmCallLog callLog = new LlmCallLog();
        callLog.setUserId(com.treasure.common.UserContext.getUserId());
        callLog.setModelId(modelId);
        callLog.setStream(stream);
        callLog.setPromptTokens(usage == null ? null : usage.promptTokens());
        callLog.setCompletionTokens(usage == null ? null : usage.completionTokens());
        callLog.setTotalTokens(usage == null ? null : usage.totalTokens());
        callLog.setFinishReason(finishReason);
        callLog.setStatus(status);
        callLog.setLatencyMs(System.currentTimeMillis() - startTime);
        callLog.setErrorCode(errorCode);
        callLog.setErrorMessage(errorMessage);

        try {
            llmCallLogMapper.insert(callLog);
        } catch (Exception e) {
            log.warn("写入 LLM 调用记录失败：{}", e.getMessage(), e);
        }
    }
}
