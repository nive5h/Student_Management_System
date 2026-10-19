package com.sms.dao;

import com.sms.model.Student;
import com.sms.util.DBConnection;
import com.sms.util.LoggerUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object managing all CRUD operations, filtering, sorting,
 * pagination, and aggregation metrics for Student entities.
 */
public class StudentDAO {

    /**
     * Adds a new student record using parameterized query.
     * Catches unique constraint violations and throws DuplicateRollException.
     */
    public boolean addStudent(Student student) throws DuplicateRollException, SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            String sql = "INSERT INTO students (uname, rno, dept, email, phone) VALUES (?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, student.getUname());
            ps.setString(2, student.getRno());
            ps.setString(3, student.getDept());
            ps.setString(4, student.getEmail().isEmpty() ? null : student.getEmail());
            ps.setString(5, student.getPhone().isEmpty() ? null : student.getPhone());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Oracle ORA-00001 is Unique Constraint Violated
            if (e.getErrorCode() == 1 || (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique constraint"))) {
                throw new DuplicateRollException("A student with Roll Number '" + student.getRno() + "' already exists.");
            }
            LoggerUtil.error("Error inserting student: " + student.getRno(), e);
            throw e;
        } finally {
            DBConnection.close(con, ps, null);
        }
    }

    /**
     * Updates an existing student record by roll number.
     */
    public boolean updateStudent(Student student) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            String sql = "UPDATE students SET uname = ?, dept = ?, email = ?, phone = ? WHERE rno = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, student.getUname());
            ps.setString(2, student.getDept());
            ps.setString(3, student.getEmail().isEmpty() ? null : student.getEmail());
            ps.setString(4, student.getPhone().isEmpty() ? null : student.getPhone());
            ps.setString(5, student.getRno());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error updating student: " + student.getRno(), e);
            throw e;
        } finally {
            DBConnection.close(con, ps, null);
        }
    }

    /**
     * Deletes a student record by roll number.
     */
    public boolean deleteStudent(String rno) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            String sql = "DELETE FROM students WHERE rno = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, rno);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error deleting student: " + rno, e);
            throw e;
        } finally {
            DBConnection.close(con, ps, null);
        }
    }

    /**
     * Fetches a student by roll number.
     */
    public Student getStudentByRno(String rno) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            String sql = "SELECT uname, rno, dept, email, phone FROM students WHERE rno = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, rno);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new Student(
                    rs.getString("rno"),
                    rs.getString("uname"),
                    rs.getString("dept"),
                    rs.getString("email"),
                    rs.getString("phone")
                );
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error getting student by roll no: " + rno, e);
        } finally {
            DBConnection.close(con, ps, rs);
        }
        return null;
    }

    /**
     * Retrieves paginated students with optional search filtering and sorting.
     */
    public List<Student> getStudents(String search, String deptFilter, String sortBy, String sortOrder, int page, int pageSize) {
        List<Student> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        // Whitelist sorting parameters to avoid SQL injection
        String validSortBy = "uname";
        if ("rno".equalsIgnoreCase(sortBy)) validSortBy = "rno";
        else if ("dept".equalsIgnoreCase(sortBy)) validSortBy = "dept";
        else if ("email".equalsIgnoreCase(sortBy)) validSortBy = "email";
        else if ("phone".equalsIgnoreCase(sortBy)) validSortBy = "phone";

        String validOrder = "ASC";
        if ("DESC".equalsIgnoreCase(sortOrder)) {
            validOrder = "DESC";
        }

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        List<String> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            whereClause.append(" AND (LOWER(uname) LIKE ? OR LOWER(rno) LIKE ?) ");
            String term = "%" + search.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (deptFilter != null && !deptFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(deptFilter)) {
            whereClause.append(" AND dept = ? ");
            params.add(deptFilter.trim());
        }

        int startRow = (page - 1) * pageSize;
        int endRow = page * pageSize;

        // Oracle standard ROWNUM pagination
        String sql = "SELECT * FROM ( " +
                     "  SELECT a.*, ROWNUM rnum FROM ( " +
                     "    SELECT uname, rno, dept, email, phone FROM students " +
                     whereClause.toString() +
                     "    ORDER BY " + validSortBy + " " + validOrder +
                     "  ) a WHERE ROWNUM <= ? " +
                     ") WHERE rnum > ?";

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            int idx = 1;
            for (String param : params) {
                ps.setString(idx++, param);
            }
            ps.setInt(idx++, endRow);
            ps.setInt(idx, startRow);

            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Student(
                    rs.getString("rno"),
                    rs.getString("uname"),
                    rs.getString("dept"),
                    rs.getString("email"),
                    rs.getString("phone")
                ));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error querying students list", e);
        } finally {
            DBConnection.close(con, ps, rs);
        }
        return list;
    }

    /**
     * Counts the total number of students matching the given search and department filter.
     */
    public int getTotalCount(String search, String deptFilter) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        List<String> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            whereClause.append(" AND (LOWER(uname) LIKE ? OR LOWER(rno) LIKE ?) ");
            String term = "%" + search.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (deptFilter != null && !deptFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(deptFilter)) {
            whereClause.append(" AND dept = ? ");
            params.add(deptFilter.trim());
        }

        String sql = "SELECT COUNT(*) FROM students " + whereClause.toString();

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(sql);
            int idx = 1;
            for (String param : params) {
                ps.setString(idx++, param);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error getting total student count", e);
        } finally {
            DBConnection.close(con, ps, rs);
        }
        return 0;
    }

    /**
     * Returns student count grouped by department for dashboard statistics.
     */
    public Map<String, Integer> getDepartmentCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT dept, COUNT(*) AS total FROM students GROUP BY dept ORDER BY dept");
            while (rs.next()) {
                String d = rs.getString("dept");
                if (d == null || d.trim().isEmpty()) {
                    d = "Unassigned";
                }
                counts.put(d, rs.getInt("total"));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error getting department counts", e);
        } finally {
            DBConnection.close(con, stmt, rs);
        }
        return counts;
    }

    /**
     * Fetches all unique departments for filter dropdowns.
     */
    public List<String> getDistinctDepartments() {
        List<String> depts = new ArrayList<>();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT DISTINCT dept FROM students WHERE dept IS NOT NULL ORDER BY dept");
            while (rs.next()) {
                String d = rs.getString("dept");
                if (d != null && !d.trim().isEmpty()) {
                    depts.add(d);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error getting distinct departments", e);
        } finally {
            DBConnection.close(con, stmt, rs);
        }
        return depts;
    }
}
