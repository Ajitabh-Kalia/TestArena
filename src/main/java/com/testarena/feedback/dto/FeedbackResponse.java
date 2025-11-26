package com.testarena.feedback.dto;

import java.util.List;

public class FeedbackResponse {
    private List<FeedbackTopic> topics;
    private String message;

    public List<FeedbackTopic> getTopics() { return topics; }
    public void setTopics(List<FeedbackTopic> topics) { this.topics = topics; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
