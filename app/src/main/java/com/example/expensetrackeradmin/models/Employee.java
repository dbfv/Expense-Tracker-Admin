package com.example.expensetrackeradmin.models;

import java.util.ArrayList;
import java.util.List;

public class Employee {
    private String id;
    private String name;
    private String code;
    private String email;
    private String role;
    private List<String> joinedProjects;
    private List<String> favoriteProjects;

    public Employee() {
        this.joinedProjects = new ArrayList<>();
        this.favoriteProjects = new ArrayList<>();
    }

    public Employee(String id, String name, String code, String email) {
        this(id, name, code, email, "employee");
    }

    public Employee(String id, String name, String code, String email, String role) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.email = email;
        this.role = role;
        this.joinedProjects = new ArrayList<>();
        this.favoriteProjects = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getJoinedProjects() {
        return joinedProjects != null ? joinedProjects : new ArrayList<>();
    }
    public void setJoinedProjects(List<String> joinedProjects) {
        this.joinedProjects = joinedProjects;
    }

    public List<String> getFavoriteProjects() {
        return favoriteProjects != null ? favoriteProjects : new ArrayList<>();
    }
    public void setFavoriteProjects(List<String> favoriteProjects) {
        this.favoriteProjects = favoriteProjects;
    }
}