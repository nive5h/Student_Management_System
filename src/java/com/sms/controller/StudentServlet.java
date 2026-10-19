package com.sms.controller;

import com.sms.dao.DuplicateRollException;
import com.sms.dao.StudentDAO;
import com.sms.model.Student;
import com.sms.util.LoggerUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Controller managing Student lifecycle: listing, searching, pagination,
 * sorting, creation, updating, and secure POST-only deletion.
 */
@WebServlet(name = "StudentServlet", urlPatterns = {"/Student", "/Info", "/Ins"})
public class StudentServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private static final int DEFAULT_PAGE_SIZE = 5;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10}$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty() || "list".equalsIgnoreCase(action) || "View Student".equalsIgnoreCase(action)) {
            handleList(request, response);
        } else if ("add".equalsIgnoreCase(action) || "Add Student".equalsIgnoreCase(action)) {
            handleAdd(request, response);
        } else if ("edit".equalsIgnoreCase(action)) {
            handleEdit(request, response);
        } else if ("delete".equalsIgnoreCase(action)) {
            // Reject GET-based deletions as required by report security specifications
            LoggerUtil.warning("Blocked attempt to trigger deletion via HTTP GET.");
            request.getSession().setAttribute("flashError", "Security Warning: Deletions are only accepted through POST requests.");
            response.sendRedirect(request.getContextPath() + "/Student?action=list");
        } else {
            handleList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("insert".equalsIgnoreCase(action) || "Submit Registration".equalsIgnoreCase(action)) {
            handleInsert(request, response);
        } else if ("update".equalsIgnoreCase(action) || "UpdateRecord".equalsIgnoreCase(action)) {
            handleUpdate(request, response);
        } else if ("delete".equalsIgnoreCase(action)) {
            handleDelete(request, response);
        } else if ("Add Student".equalsIgnoreCase(action)) {
            handleAdd(request, response);
        } else if ("View Student".equalsIgnoreCase(action)) {
            response.sendRedirect(request.getContextPath() + "/Student?action=list");
        } else {
            response.sendRedirect(request.getContextPath() + "/Student?action=list");
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("search");
        String dept = request.getParameter("dept");
        String sortBy = request.getParameter("sortBy");
        String sortOrder = request.getParameter("sortOrder");

        int page = 1;
        try {
            String pageStr = request.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            }
        } catch (NumberFormatException ignored) {
            page = 1;
        }

        int totalCount = studentDAO.getTotalCount(search, dept);
        int totalPages = (int) Math.ceil((double) totalCount / DEFAULT_PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        List<Student> students = studentDAO.getStudents(search, dept, sortBy, sortOrder, page, DEFAULT_PAGE_SIZE);
        List<String> departments = studentDAO.getDistinctDepartments();

        request.setAttribute("students", students);
        request.setAttribute("departments", departments);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("search", search != null ? search : "");
        request.setAttribute("selectedDept", dept != null ? dept : "ALL");
        request.setAttribute("sortBy", sortBy != null ? sortBy : "uname");
        request.setAttribute("sortOrder", sortOrder != null ? sortOrder : "ASC");

        request.getRequestDispatcher("/student-list.jsp").forward(request, response);
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("formMode", "add");
        request.setAttribute("student", new Student());
        request.getRequestDispatcher("/student-form.jsp").forward(request, response);
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String rno = request.getParameter("rno");
        if (rno == null || rno.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/Student?action=list");
            return;
        }

        Student student = studentDAO.getStudentByRno(rno.trim());
        if (student == null) {
            request.getSession().setAttribute("flashError", "Student record not found for Roll No: " + rno);
            response.sendRedirect(request.getContextPath() + "/Student?action=list");
            return;
        }

        request.setAttribute("formMode", "edit");
        request.setAttribute("student", student);
        request.getRequestDispatcher("/student-form.jsp").forward(request, response);
    }

    private void handleInsert(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uname = request.getParameter("uname");
        String rno = request.getParameter("rno");
        String dept = request.getParameter("dept");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        Student student = new Student(rno, uname, dept, email, phone);
        List<String> errors = validateStudent(student, true);

        if (!errors.isEmpty()) {
            request.setAttribute("validationErrors", errors);
            request.setAttribute("student", student);
            request.setAttribute("formMode", "add");
            request.getRequestDispatcher("/student-form.jsp").forward(request, response);
            return;
        }

        try {
            studentDAO.addStudent(student);
            LoggerUtil.info("Added new student record: " + student.getRno() + " (" + student.getUname() + ")");
            request.getSession().setAttribute("flashSuccess", "Student registered successfully!");
            response.sendRedirect(request.getContextPath() + "/Student?action=list");
        } catch (DuplicateRollException e) {
            errors.add(e.getMessage());
            request.setAttribute("validationErrors", errors);
            request.setAttribute("student", student);
            request.setAttribute("formMode", "add");
            request.getRequestDispatcher("/student-form.jsp").forward(request, response);
        } catch (SQLException e) {
            LoggerUtil.error("Failed to insert student", e);
            errors.add("A database error occurred while registering the student. Please try again.");
            request.setAttribute("validationErrors", errors);
            request.setAttribute("student", student);
            request.setAttribute("formMode", "add");
            request.getRequestDispatcher("/student-form.jsp").forward(request, response);
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String rno = request.getParameter("rno");
        String uname = request.getParameter("uname");
        String dept = request.getParameter("dept");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        Student student = new Student(rno, uname, dept, email, phone);
        List<String> errors = validateStudent(student, false);

        if (!errors.isEmpty()) {
            request.setAttribute("validationErrors", errors);
            request.setAttribute("student", student);
            request.setAttribute("formMode", "edit");
            request.getRequestDispatcher("/student-form.jsp").forward(request, response);
            return;
        }

        try {
            studentDAO.updateStudent(student);
            LoggerUtil.info("Updated student record: " + student.getRno());
            request.getSession().setAttribute("flashSuccess", "Student record updated successfully!");
            response.sendRedirect(request.getContextPath() + "/Student?action=list");
        } catch (SQLException e) {
            LoggerUtil.error("Failed to update student", e);
            errors.add("A database error occurred while updating the record.");
            request.setAttribute("validationErrors", errors);
            request.setAttribute("student", student);
            request.setAttribute("formMode", "edit");
            request.getRequestDispatcher("/student-form.jsp").forward(request, response);
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("userRole") : null;

        // Role check: Only Admin is authorized to delete
        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
            session.setAttribute("flashError", "Access Denied: Only Administrators are permitted to delete records.");
            response.sendRedirect(request.getContextPath() + "/Student?action=list");
            return;
        }

        String rno = request.getParameter("rno");
        if (rno != null && !rno.trim().isEmpty()) {
            try {
                studentDAO.deleteStudent(rno.trim());
                LoggerUtil.info("Deleted student record: " + rno);
                session.setAttribute("flashSuccess", "Student record (" + rno + ") deleted successfully.");
            } catch (SQLException e) {
                LoggerUtil.error("Failed to delete student: " + rno, e);
                session.setAttribute("flashError", "Could not delete student record due to database error.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/Student?action=list");
    }

    private List<String> validateStudent(Student s, boolean isNew) {
        List<String> errors = new ArrayList<>();
        if (s.getUname() == null || s.getUname().trim().isEmpty()) {
            errors.add("Student Name is required.");
        }
        if (isNew && (s.getRno() == null || s.getRno().trim().isEmpty())) {
            errors.add("Roll Number is required.");
        }
        if (s.getDept() == null || s.getDept().trim().isEmpty()) {
            errors.add("Department is required.");
        }
        if (!s.getEmail().isEmpty() && !EMAIL_PATTERN.matcher(s.getEmail()).matches()) {
            errors.add("Please provide a valid email address (e.g., student@domain.com).");
        }
        if (!s.getPhone().isEmpty() && !PHONE_PATTERN.matcher(s.getPhone()).matches()) {
            errors.add("Phone number must be exactly 10 digits.");
        }
        return errors;
    }
}
