package model;

import java.util.Date;

public class Bill {
    private int billId;
    private int patientId;
    private String patientName;
    private double bedCharge;
    private double serviceCharge;
    private double doctorFee;
    private double total;
    private Date billDate;

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public double getBedCharge() { return bedCharge; }
    public void setBedCharge(double bedCharge) { this.bedCharge = bedCharge; }
    public double getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(double serviceCharge) { this.serviceCharge = serviceCharge; }
    public double getDoctorFee() { return doctorFee; }
    public void setDoctorFee(double doctorFee) { this.doctorFee = doctorFee; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public Date getBillDate() { return billDate; }
    public void setBillDate(Date billDate) { this.billDate = billDate; }
}