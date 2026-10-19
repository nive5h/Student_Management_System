package com.sms.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Authentication and Authorization Filter.
 * Protects application endpoints, ensures only authenticated users can access resources,
 * and restricts sensitive actions (such as student deletion) to Administrators.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        // Allow static resources and public login endpoints
        boolean isPublic = path.equals("") ||
                           path.equals("/") ||
                           path.equals("/index.html") ||
                           path.equals("/index.jsp") ||
                           path.equals("/login.jsp") ||
                           path.equals("/Login") ||
                           path.startsWith("/style.css") ||
                           path.startsWith("/css/") ||
                           path.startsWith("/js/") ||
                           path.startsWith("/images/");

        if (isPublic) {
            chain.doFilter(req, res);
            return;
        }

        // Check authentication
        HttpSession session = request.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("loggedUser") != null);

        if (!isLoggedIn) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+log+in+to+access+the+system.");
            return;
        }

        // Prevent browser caching for protected pages
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Role-based access control: Only Admin can delete records
        String action = request.getParameter("action");
        if ("delete".equalsIgnoreCase(action)) {
            String role = (String) session.getAttribute("userRole");
            if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
                request.getSession().setAttribute("flashError", "Access Denied: Only Administrators are permitted to delete student records.");
                response.sendRedirect(request.getContextPath() + "/Student?action=list");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    }
}
