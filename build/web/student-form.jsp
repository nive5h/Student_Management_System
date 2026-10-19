<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${formMode == 'edit' ? 'Edit Student' : 'Add New Student'}" /> - Student Management System</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="form-wrapper">
        <header class="app-header" style="margin-bottom: 20px;">
            <div>
                <h1>Student Management System</h1>
                <p class="subtitle"><c:out value="${formMode == 'edit' ? 'Modify Student Information' : 'Register New Student'}" /></p>
            </div>
            <div class="user-badge">
                <a href="Student?action=list" class="btn btn-secondary">Student Records</a>
                <a href="Dashboard" class="btn btn-secondary">Dashboard</a>
            </div>
        </header>

        <!-- Validation Error Alert Box -->
        <c:if test="${not empty validationErrors}">
            <div class="alert alert-danger">
                <p style="font-weight: bold; margin-bottom: 8px;">Please correct the following errors:</p>
                <ul style="margin: 0; padding-left: 20px;">
                    <c:forEach var="err" items="${validationErrors}">
                        <li><c:out value="${err}" /></li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>

        <div class="panel form-panel">
            <h2><c:out value="${formMode == 'edit' ? 'Edit Student Details' : 'Student Registration Form'}" /></h2>
            
            <form action="Student" method="POST" autocomplete="off" class="student-crud-form">
                <input type="hidden" name="action" value="<c:out value="${formMode == 'edit' ? 'update' : 'insert'}" />">

                <div class="form-row">
                    <label for="rno">Roll Number <span class="required-star">*</span></label>
                    <c:choose>
                        <c:when test="${formMode == 'edit'}">
                            <input type="text" id="rno" name="rno" value="<c:out value='${student.rno}' />" readonly class="input-readonly">
                            <small class="field-hint">Roll Number cannot be modified once created.</small>
                        </c:when>
                        <c:otherwise>
                            <input type="text" id="rno" name="rno" value="<c:out value='${student.rno}' />" required placeholder="e.g., 24SUCS60">
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="form-row">
                    <label for="uname">Full Name <span class="required-star">*</span></label>
                    <input type="text" id="uname" name="uname" value="<c:out value='${student.uname}' />" required placeholder="Enter student's full name">
                </div>

                <div class="form-row">
                    <label for="dept">Department <span class="required-star">*</span></label>
                    <input type="text" id="dept" name="dept" value="<c:out value='${student.dept}' />" required placeholder="e.g., Computer Science">
                </div>

                <div class="form-row">
                    <label for="email">Email Address</label>
                    <input type="email" id="email" name="email" value="<c:out value='${student.email}' />" placeholder="e.g., student@college.edu">
                </div>

                <div class="form-row">
                    <label for="phone">Phone Number (10 digits)</label>
                    <input type="text" id="phone" name="phone" value="<c:out value='${student.phone}' />" maxlength="10" placeholder="e.g., 9876543210">
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">
                        <c:out value="${formMode == 'edit' ? 'Update Record' : 'Submit Registration'}" />
                    </button>
                    <a href="Student?action=list" class="btn btn-secondary">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
