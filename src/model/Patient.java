package model;

import java.util.Date;

public class Patient {
    private int patientId;
    private String name;
    private int age;
    private String gender;
    private String illness;
    private Date admittedDate;
    private int doctorId;
    private String assignedDoctorName; // For display purposes
    private int bedId; // NEW FIELD

    // Getters and Setters...
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getIllness() { return illness; }
    public void setIllness(String illness) { this.illness = illness; }
    public Date getAdmittedDate() { return admittedDate; }
    public void setAdmittedDate(Date admittedDate) { this.admittedDate = admittedDate; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getAssignedDoctorName() { return assignedDoctorName; }
    public void setAssignedDoctorName(String assignedDoctorName) { this.assignedDoctorName = assignedDoctorName; }
    public int getBedId() { return bedId; } // NEW GETTER
    public void setBedId(int bedId) { this.bedId = bedId; } // NEW SETTER
}