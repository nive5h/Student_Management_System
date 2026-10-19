<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Student Management System</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="dashboard-wrapper">
        <header class="app-header">
            <div>
                <h1>Student Management System</h1>
                <p class="subtitle">System Overview &amp; Metrics</p>
            </div>
            <div class="user-badge">
                <span class="user-greeting">Welcome, <b><c:out value="${sessionScope.loggedUser}" /></b></span>
                <span class="role-tag <c:out value="${sessionScope.userRole == 'ADMIN' ? 'role-admin' : 'role-staff'}" />">
                    <c:out value="${sessionScope.userRole}" />
                </span>
                <a href="Logout" class="btn btn-logout">Logout</a>
            </div>
        </header>

        <!-- Flash messages -->
        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="alert alert-success">
                <c:out value="${sessionScope.flashSuccess}" />
            </div>
            <c:remove var="flashSuccess" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="alert alert-danger">
                <c:out value="${sessionScope.flashError}" />
            </div>
            <c:remove var="flashError" scope="session" />
        </c:if>

        <!-- Metric Cards -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">🎓</div>
                <div class="stat-info">
                    <h3>Total Enrolled</h3>
                    <p class="stat-number"><c:out value="${totalStudents}" /></p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">🏛️</div>
                <div class="stat-info">
                    <h3>Active Departments</h3>
                    <p class="stat-number"><c:out value="${deptCounts != null ? deptCounts.size() : 0}" /></p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">🔒</div>
                <div class="stat-info">
                    <h3>Permission Level</h3>
                    <p class="stat-number" style="font-size: 20px;"><c:out value="${sessionScope.userRole}" /></p>
                </div>
            </div>
        </div>

        <!-- Department Breakdown Section -->
        <div class="panel">
            <h2>Department Distribution</h2>
            <c:choose>
                <c:when test="${not empty deptCounts}">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Department Name</th>
                                <th style="text-align: right;">Student Count</th>
                                <th style="text-align: right;">Percentage</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="entry" items="${deptCounts}">
                                <tr>
                                    <td><b><c:out value="${entry.key}" /></b></td>
                                    <td style="text-align: right;"><c:out value="${entry.value}" /></td>
                                    <td style="text-align: right;">
                                        <c:choose>
                                            <c:when test="${totalStudents > 0}">
                                                <c:out value="${String.format('%.1f', (entry.value * 100.0) / totalStudents)}" />%
                                            </c:when>
                                            <c:otherwise>0%</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p style="color: #636e72; padding: 15px 0;">No student records currently in the system.</p>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Action Quick-links -->
        <div class="quick-actions">
            <a href="Student?action=add" class="btn btn-action">➕ Add New Student</a>
            <a href="Student?action=list" class="btn btn-action">📋 View Student Records</a>
        </div>
    </div>
</body>
</html>
