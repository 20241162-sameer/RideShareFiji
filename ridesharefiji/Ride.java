package com.example.ridesharefiji;

public class Ride {

    private String id;
    private String from;
    private String to;
    private String date;
    private String time;
    private String seats;
    private String driverName;
    private String driverContact;
    private String vehicleReg;
    private String userId;
    private String fare;


    // ✅ Required empty constructor for Firebase
    public Ride() {
    }

    // ✅ Full constructor
    public Ride(String id, String from, String to, String date, String time, String seats, String driverName, String driverContact, String vehicleReg, String userId, String fare) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.date = date;
        this.time = time;
        this.seats = seats;
        this.driverName = driverName;
        this.driverContact = driverContact;
        this.vehicleReg = vehicleReg;
        this.userId = userId;
        this.fare = fare;
    }


    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getSeats() {
        return seats;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverContact() {
        return driverContact;
    }

    public String getVehicleReg() {
        return vehicleReg;
    }

    public String getUserId() {
        return userId;
    }

    public String getFare() {
        return fare;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setDriverContact(String driverContact) {
        this.driverContact = driverContact;
    }

    public void setVehicleReg(String vehicleReg) {
        this.vehicleReg = vehicleReg;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }
}
