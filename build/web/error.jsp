<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - Student Management System</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="container" style="max-width: 550px; text-align: center;">
        <h1 style="color: #e74c3c; margin-bottom: 10px;">Application Notice</h1>
        <div class="alert alert-danger" style="margin: 20px 0;">
            <p style="font-weight: 600; font-size: 15px;">The requested operation could not be completed.</p>
            <p style="font-size: 13px; color: #555;">An unexpected condition occurred. The event has been logged for system administration.</p>
        </div>
        <div style="margin-top: 25px;">
            <a href="Dashboard" class="btn">Return to Dashboard</a>
            <a href="login.jsp" class="btn" style="background-color: #636e72; margin-left: 10px;">Go to Login</a>
        </div>
    </div>
</body>
</html>
