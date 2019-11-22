package com.right.ayomide.tabianconsulting.models.fcm;

public class FirebaseCloudMessage {
    private String to;
    private String data;

    public FirebaseCloudMessage() {
    }

    public FirebaseCloudMessage(String to, String data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "FirebaseCloudMessage{" +
                "to='" + to + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
