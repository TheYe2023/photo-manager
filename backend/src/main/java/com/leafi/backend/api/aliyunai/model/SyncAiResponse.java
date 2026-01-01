package com.leafi.backend.api.aliyunai.model;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SyncAiResponse {
    private Output output;
    private String request_id;

    @Data
    public static class Output {
        private List<Choice> choices;
    }

    @Data
    public static class Choice {
        private String finish_reason;
        private Message message;
    }

    @Data
    public static class Message {
        private String role;
        private List<Map<String, String>> content;
    }
}
