package com.testarena.feedback.dto;

import java.util.List;

public class FeedbackTopic {
    private String topicName;
    private String feedback;
    private List<String> recommendedLinks;

    // getters & setters
    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public List<String> getRecommendedLinks() { return recommendedLinks; }
    public void setRecommendedLinks(List<String> recommendedLinks) { this.recommendedLinks = recommendedLinks; }
}
