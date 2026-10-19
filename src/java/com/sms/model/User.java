package com.sms.model;

import java.io.Serializable;

/**
 * Model class representing a User entity with role-based access.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uname;
    private String password;
    private String salt;
    private String role; // "ADMIN" or "STAFF"

    public User() {
    }

    public User(String uname, String password, String salt, String role) {
        this.uname = uname;
        this.password = password;
        this.salt = salt;
        this.role = role;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getRole() {
        return role != null ? role.toUpperCase() : "STAFF";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }
}
