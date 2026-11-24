package com.treasure.llmserver.mapper;

import com.treasure.llmserver.record.LlmCallLog;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LlmCallLogMapper {

    @Insert("""
            INSERT INTO llm_call_log(
                user_id,
                model_id,
                stream,
                prompt_tokens,
                completion_tokens,
                total_tokens,
                finish_reason,
                status,
                latency_ms,
                error_code,
                error_message
            ) VALUES (
                #{userId},
                #{modelId},
                #{stream},
                #{promptTokens},
                #{completionTokens},
                #{totalTokens},
                #{finishReason},
                #{status},
                #{latencyMs},
                #{errorCode},
                #{errorMessage}
            )
            """)
    int insert(LlmCallLog log);
}
