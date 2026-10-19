package com.sms.controller;

import com.sms.dao.UserDAO;
import com.sms.model.User;
import com.sms.util.LoggerUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Controller handling user authentication, session creation, and role assignment.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/Login"})
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // If already logged in, redirect directly to dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedUser") != null) {
            response.sendRedirect(request.getContextPath() + "/Dashboard");
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("uname");
        String password = request.getParameter("pass");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Username and password are required.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        username = username.trim();
        User user = userDAO.authenticate(username, password);

        if (user != null) {
            // Prevent Session Fixation by invalidating old session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("loggedUser", user.getUname());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("flashSuccess", "Welcome, " + user.getUname() + " (" + user.getRole() + ")");

            LoggerUtil.info("User '" + user.getUname() + "' logged in successfully as " + user.getRole());
            response.sendRedirect(request.getContextPath() + "/Dashboard");
        } else {
            LoggerUtil.warning("Failed login attempt for user: " + username);
            request.setAttribute("error", "Invalid username or password. Please verify your credentials.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
