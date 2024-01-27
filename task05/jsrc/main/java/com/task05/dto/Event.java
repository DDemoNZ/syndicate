package com.task05.dto;

public class Event {

    private String id;
    private int principalId;
    private String createdAt;
    private String body;

    public Event() {
    }

    public Event(String id, int principalId, String createdAt, String body) {
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Event withId(String id) {
        this.setId(id);
        return this;
    }

    public Event withPrincipalId(int principalId) {
        this.setPrincipalId(principalId);
        return this;
    }

    public Event withCreatedAt(String createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public Event withBody(String body) {
        this.setBody(body);
        return this;
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
