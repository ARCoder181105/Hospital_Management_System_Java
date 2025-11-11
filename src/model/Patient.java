package model;

import java.util.Date;

public class Patient {
    private int patientId;
    private String name;
    private int age;
    private String gender;
    private Date admittedDate;
    private int doctorId;
    private String diseaseSeverity;
    private int bedId; // This is the assigned bed, fetched with a join

    // [START] MODIFICATIONS
    private int illnessId; // Changed from String
    private String otherIllnessText; // New field for "Other"
    private int requestedBedTypeId; // Changed from String
    
    // Transient fields (for joins)
    private String assignedDoctorName;
    private String illnessName;
    private String requestedBedTypeName;
    private double pricePerDay;
    // [END] MODIFICATIONS

    @Override
    public String toString() {
        return name + " (ID: " + patientId + ")";
    }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Date getAdmittedDate() { return admittedDate; }
    public void setAdmittedDate(Date admittedDate) { this.admittedDate = admittedDate; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getDiseaseSeverity() { return diseaseSeverity; }
    public void setDiseaseSeverity(String diseaseSeverity) { this.diseaseSeverity = diseaseSeverity; }
    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }

    // [START] MODIFIED/NEW GETTERS/SETTERS
    public int getIllnessId() { return illnessId; }
    public void setIllnessId(int illnessId) { this.illnessId = illnessId; }
    public String getOtherIllnessText() { return otherIllnessText; }
    public void setOtherIllnessText(String otherIllnessText) { this.otherIllnessText = otherIllnessText; }
    public int getRequestedBedTypeId() { return requestedBedTypeId; }
    public void setRequestedBedTypeId(int requestedBedTypeId) { this.requestedBedTypeId = requestedBedTypeId; }
    
    // Transient fields
    public String getAssignedDoctorName() { return assignedDoctorName; }
    public void setAssignedDoctorName(String assignedDoctorName) { this.assignedDoctorName = assignedDoctorName; }
    public String getIllnessName() { return illnessName; }
    public void setIllnessName(String illnessName) { this.illnessName = illnessName; }
    public String getRequestedBedTypeName() { return requestedBedTypeName; }
    public void setRequestedBedTypeName(String requestedBedTypeName) { this.requestedBedTypeName = requestedBedTypeName; }
    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    // [END] MODIFIED/NEW GETTERS/SETTERS
}