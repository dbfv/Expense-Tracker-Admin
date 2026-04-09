package com.example.expensetrackeradmin.models;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class Project {
    private String projectId;
    private String name;
    private String password;
    private String passwordHash;
    private String description;
    private String startDate;
    private String endDate;
    private String manager;
    private String status;
    private double budget;
    private String specialRequirements;
    private String clientInfo;
    private double spentAmount = 0.0;

    public Project() {
    }

    public Project(String projectId, String name, String password, String passwordHash, String description, String startDate,
                   String endDate, String manager, String status, double budget,
                   String specialRequirements, String clientInfo) {
        this.projectId = projectId;
        this.name = name;
        this.password = password;
        this.passwordHash = passwordHash;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.manager = manager;
        this.status = status;
        this.budget = budget;
        this.specialRequirements = specialRequirements;
        this.clientInfo = clientInfo;
    }

    // Getters and Setters
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }

    public String getClientInfo() { return clientInfo; }
    public void setClientInfo(String clientInfo) { this.clientInfo = clientInfo; }

    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }

}