package com.example.ridesharefiji;

public class RideRequest {
    private String id;
    private String from;
    private String to;
    private String date;
    private String userId;
    private String passengerName;
    private String passengerPhone;

    public RideRequest() {}

    public RideRequest(String id, String from, String to, String date, String userId, String passengerName, String passengerPhone) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.date = date;
        this.userId = userId;
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public String getPassengerPhone() { return passengerPhone; }
    public void setPassengerPhone(String passengerPhone) { this.passengerPhone = passengerPhone; }
}