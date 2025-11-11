package model;

public class Bed {
    private int bedId;
    private String ward;
    private String status;
    private int patientId;
    private String patientName;
    private int floor;
    
    // [START] MODIFICATIONS
    private int bedTypeId; // Changed from String
    
    // Transient fields (for joins, not in 'beds' table)
    private String bedTypeName;
    private double pricePerDay;
    // [END] MODIFICATIONS

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
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    
    // [START] MODIFIED/NEW GETTERS/SETTERS
    public int getBedTypeId() { return bedTypeId; }
    public void setBedTypeId(int bedTypeId) { this.bedTypeId = bedTypeId; }
    public String getBedTypeName() { return bedTypeName; }
    public void setBedTypeName(String bedTypeName) { this.bedTypeName = bedTypeName; }
    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    // [END] MODIFIED/NEW GETTERS/SETTERS
}