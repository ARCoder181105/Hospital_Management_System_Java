package model;

public class Doctor {
    private int doctorId;
    private String name;
    private String specialization;
    private String phone;
    private String email;

    // Override toString for easy display in JComboBox
    @Override
    public String toString() {
        return name + " (" + specialization + ")";
    }

    // Getters and Setters
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
}