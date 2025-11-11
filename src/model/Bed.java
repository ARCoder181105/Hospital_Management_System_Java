package model;

public class Bed {
    private int bedId;
    private String ward;
    private String status;
    private int patientId;
    private String patientName;

    // [START] NEW FIELDS
    private int floor;
    private String bedType;
    private double pricePerDay;
    // [END] NEW FIELDS

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
    
    // [START] NEW GETTERS/SETTERS
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }
    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    // [END] NEW GETTERS/SETTERS
}