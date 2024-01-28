package com.task05.dto;

import java.util.Map;

public class Event {

    private String id;
    private int principalId;
    private String createdAt;
    private Map<String, String> body;

    public Event() {
    }

    public Event(String id, int principalId, String createdAt, Map<String, String> body) {
        this.id = id;
        this.principalId = principalId;
        this.createdAt = createdAt;
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, String> getBody() {
        return body;
    }

    public void setBody(Map<String, String> body) {
        this.body = body;
    }

    public Event with() {
        return this;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", principalId=" + principalId +
                ", createdAt='" + createdAt + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
