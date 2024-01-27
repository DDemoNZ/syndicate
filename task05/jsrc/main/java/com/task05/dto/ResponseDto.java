package com.task05.dto;

public class ResponseDto {

    private int statusCode;
    private Event event;

    public ResponseDto() {
    }

    public ResponseDto(int statusCode, Event event) {
        this.statusCode = statusCode;
        this.event = event;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "ResponseDto{" +
                "statusCode=" + statusCode +
                ", event=" + event +
                '}';
    }

    public ResponseDto withStatusCode(int statusCode) {
        this.setStatusCode(statusCode);
        return this;
    }

    public ResponseDto withEvent(Event content) {
        this.setEvent(content);
        return this;
    }
}
