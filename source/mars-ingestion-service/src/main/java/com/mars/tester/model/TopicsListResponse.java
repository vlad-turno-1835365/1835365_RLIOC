package com.mars.tester.model;

import java.util.List;

public class TopicsListResponse {
    private List<String> topics;

    public TopicsListResponse() {}

    public TopicsListResponse(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    @Override
    public String toString() {
        return "TopicsListResponse{" +
                "topics=" + topics +
                '}';
    }
}
