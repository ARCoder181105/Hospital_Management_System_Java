package model;

public class ConfigIllness {
    private int illnessId;
    private String illnessName;

    // This is used by the JComboBox to display the text
    @Override
    public String toString() {
        return illnessName;
    }
    
    // Getters and Setters
    public int getIllnessId() { return illnessId; }
    public void setIllnessId(int illnessId) { this.illnessId = illnessId; }
    public String getIllnessName() { return illnessName; }
    public void setIllnessName(String illnessName) { this.illnessName = illnessName; }
}