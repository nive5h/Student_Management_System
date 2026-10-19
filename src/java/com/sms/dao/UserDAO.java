package com.sms.dao;

import com.sms.model.User;
import com.sms.util.DBConnection;
import com.sms.util.LoggerUtil;
import com.sms.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for User authentication and management.
 */
public class UserDAO {

    /**
     * Authenticates a user by username and raw password.
     * Verifies against salted SHA-256 hash.
     *
     * @param uname Username
     * @param rawPassword Plain text password entered by user
     * @return User object if credentials are valid, or null otherwise
     */
    public User authenticate(String uname, String rawPassword) {
        if (uname == null || rawPassword == null) {
            return null;
        }

        User user = findByUsername(uname);
        if (user == null) {
            return null;
        }

        boolean isValid = PasswordUtil.verifyPassword(rawPassword, user.getSalt(), user.getPassword());
        if (isValid) {
            return user;
        }
        return null;
    }

    /**
     * Retrieves a user by username.
     */
    public User findByUsername(String uname) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            String sql = "SELECT uname, password, salt, role FROM login WHERE uname = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, uname);
            rs = ps.executeQuery();

            if (rs.next()) {
                String u = rs.getString("uname");
                String pwd = rs.getString("password");
                String salt = rs.getString("salt");
                String role = rs.getString("role");
                if (role == null || role.trim().isEmpty()) {
                    role = "STAFF"; // Default role
                }
                return new User(u, pwd, salt, role);
            }
        } catch (SQLException e) {
            // Check if salt or role column doesn't exist yet (fallback for legacy schema)
            LoggerUtil.warning("Standard user query failed, trying legacy query fallback: " + e.getMessage());
            return findByUsernameLegacy(uname);
        } finally {
            DBConnection.close(con, ps, rs);
        }
        return null;
    }

    private User findByUsernameLegacy(String uname) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            String sql = "SELECT uname, password FROM login WHERE uname = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, uname);
            rs = ps.executeQuery();
            if (rs.next()) {
                String u = rs.getString("uname");
                String pwd = rs.getString("password");
                // Legacy plain-text row
                return new User(u, pwd, null, "ADMIN");
            }
        } catch (SQLException e) {
            LoggerUtil.error("Legacy user lookup error", e);
        } finally {
            DBConnection.close(con, ps, rs);
        }
        return null;
    }

    /**
     * Creates or updates a user with a salted password hash.
     */
    public boolean saveUserWithSalt(String uname, String rawPassword, String role) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(rawPassword, salt);

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            String sql = "MERGE INTO login l USING (SELECT ? AS u FROM dual) d " +
                         "ON (l.uname = d.u) " +
                         "WHEN MATCHED THEN UPDATE SET l.password = ?, l.salt = ?, l.role = ? " +
                         "WHEN NOT MATCHED THEN INSERT (uname, password, salt, role) VALUES (?, ?, ?, ?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, uname);
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, role);
            ps.setString(5, uname);
            ps.setString(6, hash);
            ps.setString(7, salt);
            ps.setString(8, role);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error saving user with salt: " + e.getMessage(), e);
            return false;
        } finally {
            DBConnection.close(con, ps, null);
        }
    }
}
