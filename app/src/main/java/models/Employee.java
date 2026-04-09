package models;
public class Employee {
    private String id;
    private String name;
    private String code;
    private String email;
    private String role;

    // Empty constructor required for some frameworks
    public Employee() {
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
    }

    // Getters and Setters
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
}