package model;

public class Bed {
    private int bedId;
    private String ward;
    private String status;
    private int patientId;
    private String patientName; // For display purposes

    // Getters and Setters...
    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
}