package com.sms.util;

import com.sms.dao.DuplicateRollException;
import com.sms.dao.StudentDAO;
import com.sms.dao.UserDAO;
import com.sms.model.Student;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility tool that verifies and seeds Oracle XE schema, accounts, and sample students.
 */
public class DatabaseSeeder {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  Student Management System - Database Seeder");
        System.out.println("=================================================");

        try (Connection con = DBConnection.getConnection()) {
            System.out.println("[1/4] Connected successfully to Oracle Database!");

            // 1. Verify / upgrade LOGIN table
            try (Statement st = con.createStatement()) {
                // Ensure columns exist in login table
                try {
                    st.executeUpdate("ALTER TABLE login ADD (salt VARCHAR2(64), role VARCHAR2(20) DEFAULT 'STAFF')");
                    System.out.println("      Added 'salt' and 'role' columns to LOGIN table.");
                } catch (SQLException e) {
                    // Columns might already exist
                    if (!e.getMessage().contains("ORA-01430")) {
                        // ignore column already exists
                    }
                }

                try {
                    st.executeUpdate("ALTER TABLE login MODIFY (password VARCHAR2(128))");
                    System.out.println("      Expanded 'password' column to VARCHAR2(128) in LOGIN table.");
                } catch (SQLException ignored) {
                }
            }

            // 2. Seed / update standard users
            System.out.println("[2/4] Seeding secure accounts with salted SHA-256 hashes...");
            UserDAO userDAO = new UserDAO();
            
            userDAO.saveUserWithSalt("admin", "admin123", "ADMIN");
            System.out.println("      -> Created / updated user: admin (Role: ADMIN, Password: admin123)");

            userDAO.saveUserWithSalt("staff", "staff123", "STAFF");
            System.out.println("      -> Created / updated user: staff (Role: STAFF, Password: staff123)");

            userDAO.saveUserWithSalt("nive5h", "Nivesh@2006", "ADMIN");
            System.out.println("      -> Created / updated user: nive5h (Role: ADMIN, Password: Nivesh@2006)");

            // 3. Ensure STUDENTS table structure
            System.out.println("[3/4] Checking STUDENTS table...");
            try (Statement st = con.createStatement()) {
                st.executeUpdate("CREATE TABLE students (" +
                        "uname VARCHAR2(100) NOT NULL, " +
                        "rno VARCHAR2(20) NOT NULL PRIMARY KEY, " +
                        "dept VARCHAR2(50) NOT NULL, " +
                        "email VARCHAR2(100), " +
                        "phone VARCHAR2(20))");
                System.out.println("      Created STUDENTS table.");
            } catch (SQLException e) {
                // Table already exists
                System.out.println("      STUDENTS table exists.");
            }

            // 4. Seed sample students if table is empty or sparse
            System.out.println("[4/4] Verifying student sample data...");
            StudentDAO studentDAO = new StudentDAO();
            seedStudent(studentDAO, new Student("24SUCS60", "NIVESH N S", "Computer Science", "24sucs60@tcarts.in", "8220191392"));
            seedStudent(studentDAO, new Student("24SUCS57", "SIVAKUMAR N K", "Electrical Department", "siva@gmail.com", "9865932195"));
            seedStudent(studentDAO, new Student("EMP001", "YOGESH N S", "Software Engineering", "yogesh1392@gmail.com", "9087130902"));
            seedStudent(studentDAO, new Student("24SUCS01", "AARAV SHARMA", "Computer Science", "aarav@tcarts.in", "9876543210"));
            seedStudent(studentDAO, new Student("24SUCS15", "PRIYA RAMAN", "Computer Science", "priya@tcarts.in", "9876543211"));
            seedStudent(studentDAO, new Student("24SUIT12", "KARTHIK V", "Information Technology", "karthik@tcarts.in", "9876543212"));
            seedStudent(studentDAO, new Student("24SUIT28", "DIVYA M", "Information Technology", "divya@tcarts.in", "9876543213"));
            seedStudent(studentDAO, new Student("24SUEC05", "ROHIT KUMAR", "Electronics", "rohit@tcarts.in", "9876543214"));

            int total = studentDAO.getTotalCount("", "ALL");
            System.out.println("=================================================");
            System.out.println("  Database Seeding Completed Successfully!");
            System.out.println("  Total Students in Database: " + total);
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("Database Seeding Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedStudent(StudentDAO dao, Student s) {
        try {
            if (dao.getStudentByRno(s.getRno()) == null) {
                dao.addStudent(s);
                System.out.println("      Added sample student: " + s.getRno() + " (" + s.getUname() + " - " + s.getDept() + ")");
            }
        } catch (DuplicateRollException | SQLException ignored) {
        }
    }
}
