package model;

public class ConfigBedType {
    private int bedTypeId;
    private String bedTypeName;
    private double pricePerDay;

    // This is used by the JComboBox to display the text
    @Override
    public String toString() {
        return String.format("%s (Rs. %.2f/day)", bedTypeName, pricePerDay);
    }
    
    // Getters and Setters
    public int getBedTypeId() { return bedTypeId; }
    public void setBedTypeId(int bedTypeId) { this.bedTypeId = bedTypeId; }
    public String getBedTypeName() { return bedTypeName; }
    public void setBedTypeName(String bedTypeName) { this.bedTypeName = bedTypeName; }
    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
}