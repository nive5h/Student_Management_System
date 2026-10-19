<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Student Management System</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="login-container">
        <h1>Student Management System</h1>
        <p class="subtitle">Secure Academic Records Portal</p>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">
                <c:out value="${error}" />
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div class="alert alert-danger">
                <c:out value="${param.error}" />
            </div>
        </c:if>

        <c:if test="${not empty param.logout}">
            <div class="alert alert-success">
                You have been logged out successfully.
            </div>
        </c:if>

        <form action="Login" method="POST" autocomplete="off">
            <div class="form-group">
                <label for="uname">Username</label>
                <input type="text" id="uname" name="uname" required placeholder="Enter username" autofocus>
            </div>
            <div class="form-group">
                <label for="pass">Password</label>
                <input type="password" id="pass" name="pass" required placeholder="Enter password">
            </div>
            <button type="submit" class="btn btn-primary" style="width: 100% !important; margin-top: 15px;">Login to Portal</button>
        </form>

        <div class="login-footer">
            <small>Default Roles: <b>admin</b> (Administrator) | <b>staff</b> (Staff)</small>
        </div>
    </div>
</body>
</html>
