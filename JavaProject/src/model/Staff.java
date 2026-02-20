package src.model;

public class Staff {
    // ATTRIBUTES
    private String id;
    private String username;
    private String password;

    // CONSTRUCTOR
    public Staff(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // METHODS
    public String getStaffID() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
