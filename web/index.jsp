<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session != null && session.getAttribute("loggedUser") != null) {
        response.sendRedirect("Dashboard");
    } else {
        response.sendRedirect("login.jsp");
    }
%>
