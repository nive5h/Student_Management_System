package com.sms.model;

import java.io.Serializable;

/**
 * Model class representing a Student entity.
 */
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private String rno;
    private String uname;
    private String dept;
    private String email;
    private String phone;

    public Student() {
    }

    public Student(String rno, String uname, String dept, String email, String phone) {
        this.rno = rno;
        this.uname = uname;
        this.dept = dept;
        this.email = email;
        this.phone = phone;
    }

    public String getRno() {
        return rno != null ? rno : "";
    }

    public void setRno(String rno) {
        this.rno = rno != null ? rno.trim() : null;
    }

    public String getUname() {
        return uname != null ? uname : "";
    }

    public void setUname(String uname) {
        this.uname = uname != null ? uname.trim() : null;
    }

    public String getDept() {
        return dept != null ? dept : "";
    }

    public void setDept(String dept) {
        this.dept = dept != null ? dept.trim() : null;
    }

    public String getEmail() {
        // Return empty string if null to prevent "null" showing in edit forms
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public String getPhone() {
        // Return empty string if null to prevent "null" showing in edit forms
        return phone != null ? phone : "";
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    @Override
    public String toString() {
        return "Student{" + "rno=" + rno + ", uname=" + uname + ", dept=" + dept + ", email=" + email + ", phone=" + phone + '}';
    }
}
