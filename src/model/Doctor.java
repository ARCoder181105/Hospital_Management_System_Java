package model;

public class Doctor {
    private int doctorId;
    private String name;
    private String specialization;
    private String phone;
    private String email;
    
    // [START] NEW FIELDS
    private double consultationFee;
    private String availableDays; // e.g., "Mon,Tue,Wed"
    // [END] NEW FIELDS

    @Override
    public String toString() {
        return name + " (" + specialization + ")";
    }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // [START] NEW GETTERS/SETTERS
    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }
    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }
    // [END] NEW GETTERS/SETTERS
}