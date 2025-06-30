package com.example.crime;

public class Report {
    public String crime, location, description, imageUrl;

    public Report() {} // Needed for Firebase

    public Report(String crime, String location, String description, String imageUrl) {
        this.crime = crime;
        this.location = location;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}
