<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Records - Student Management System</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="list-wrapper">
        <header class="app-header">
            <div>
                <h1>Student Management System</h1>
                <p class="subtitle">Student Records Directory</p>
            </div>
            <div class="user-badge">
                <span class="user-greeting">Logged in: <b><c:out value="${sessionScope.loggedUser}" /></b></span>
                <span class="role-tag <c:out value="${sessionScope.userRole == 'ADMIN' ? 'role-admin' : 'role-staff'}" />">
                    <c:out value="${sessionScope.userRole}" />
                </span>
                <a href="Dashboard" class="btn btn-secondary">Dashboard</a>
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

        <!-- Search and Filter Bar -->
        <div class="filter-card">
            <form action="Student" method="GET" class="filter-form">
                <input type="hidden" name="action" value="list">
                
                <div class="filter-item">
                    <input type="text" name="search" value="<c:out value='${search}' />" placeholder="Search by name or roll no..." style="width: 260px !important;">
                </div>

                <div class="filter-item">
                    <select name="dept" class="form-select">
                        <option value="ALL" <c:if test="${selectedDept == 'ALL'}">selected</c:if>>All Departments</option>
                        <c:forEach var="d" items="${departments}">
                            <option value="<c:out value='${d}' />" <c:if test="${selectedDept == d}">selected</c:if>>
                                <c:out value="${d}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-item">
                    <select name="sortBy" class="form-select">
                        <option value="uname" <c:if test="${sortBy == 'uname'}">selected</c:if>>Sort by Name</option>
                        <option value="rno" <c:if test="${sortBy == 'rno'}">selected</c:if>>Sort by Roll No</option>
                        <option value="dept" <c:if test="${sortBy == 'dept'}">selected</c:if>>Sort by Department</option>
                    </select>
                </div>

                <div class="filter-item">
                    <select name="sortOrder" class="form-select">
                        <option value="ASC" <c:if test="${sortOrder == 'ASC'}">selected</c:if>>Ascending</option>
                        <option value="DESC" <c:if test="${sortOrder == 'DESC'}">selected</c:if>>Descending</option>
                    </select>
                </div>

                <button type="submit" class="btn btn-filter">Filter &amp; Search</button>
                <c:if test="${not empty search || selectedDept != 'ALL'}">
                    <a href="Student?action=list" class="btn btn-reset">Clear</a>
                </c:if>
                <a href="Student?action=add" class="btn btn-primary" style="margin-left: auto;">➕ Add Student</a>
            </form>
        </div>

        <!-- Student Table -->
        <div class="panel">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                <h3 style="margin: 0; color: #2d3436;">Registered Students (${totalCount} total)</h3>
                <span style="font-size: 13px; color: #636e72;">Page ${currentPage} of ${totalPages}</span>
            </div>

            <c:choose>
                <c:when test="${not empty students}">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Roll Number</th>
                                <th>Student Name</th>
                                <th>Department</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th style="text-align: center;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="s" items="${students}">
                                <tr>
                                    <td><b><c:out value="${s.rno}" /></b></td>
                                    <td><c:out value="${s.uname}" /></td>
                                    <td><span class="dept-badge"><c:out value="${s.dept}" /></span></td>
                                    <td><c:out value="${not empty s.email ? s.email : '-'}" /></td>
                                    <td><c:out value="${not empty s.phone ? s.phone : '-'}" /></td>
                                    <td style="text-align: center; white-space: nowrap;">
                                        <!-- Edit button for all authorized users -->
                                        <a href="Student?action=edit&rno=<c:out value='${s.rno}' />" class="btn-icon" title="Edit Student">✏️ Edit</a>
                                        
                                        <!-- Delete POST form: only permitted for Admin -->
                                        <c:choose>
                                            <c:when test="${sessionScope.userRole == 'ADMIN'}">
                                                <form action="Student" method="POST" style="display: inline; margin: 0; padding: 0; background: none; box-shadow: none; width: auto;" onsubmit="return confirm('Are you sure you want to delete student &quot;<c:out value='${s.uname}' />&quot; (<c:out value='${s.rno}' />)?');">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="rno" value="<c:out value='${s.rno}' />">
                                                    <button type="submit" class="btn-icon btn-icon-delete" title="Delete Student">🗑️ Delete</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="btn-icon btn-icon-disabled" title="Only Administrators can delete records">🔒 Locked</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <!-- Pagination Navigation -->
                    <c:if test="${totalPages > 1}">
                        <div class="pagination">
                            <c:if test="${currentPage > 1}">
                                <a href="Student?action=list&search=<c:out value='${search}' />&dept=<c:out value='${selectedDept}' />&sortBy=<c:out value='${sortBy}' />&sortOrder=<c:out value='${sortOrder}' />&page=${currentPage - 1}" class="page-link">&laquo; Previous</a>
                            </c:if>
                            
                            <c:forEach var="p" begin="1" end="${totalPages}">
                                <a href="Student?action=list&search=<c:out value='${search}' />&dept=<c:out value='${selectedDept}' />&sortBy=<c:out value='${sortBy}' />&sortOrder=<c:out value='${sortOrder}' />&page=${p}" class="page-link <c:if test='${p == currentPage}'>active</c:if>">${p}</a>
                            </c:forEach>

                            <c:if test="${currentPage < totalPages}">
                                <a href="Student?action=list&search=<c:out value='${search}' />&dept=<c:out value='${selectedDept}' />&sortBy=<c:out value='${sortBy}' />&sortOrder=<c:out value='${sortOrder}' />&page=${currentPage + 1}" class="page-link">Next &raquo;</a>
                            </c:if>
                        </div>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <div style="text-align: center; padding: 40px 20px; color: #636e72;">
                        <p style="font-size: 16px;">No student records found matching the criteria.</p>
                        <a href="Student?action=list" class="btn btn-secondary">Reset Search</a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <div style="margin-top: 15px; text-align: center;">
            <a href="Dashboard" class="btn btn-secondary">⬅️ Back to Dashboard</a>
        </div>
    </div>
</body>
</html>
