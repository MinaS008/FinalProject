package Model;
import java.util.ArrayList;
import java.util.List;


public class Location extends CodexEntry {
    private static final long serialVersionUID = 1L;

    private String locationType;
    private List<String> subLocations;

    private List<String> connections;
    private String region;

    public Location(String id, String name, String description, String locationType) {
        super(id, name, description);
        this.locationType = locationType;

        this.subLocations = new ArrayList<>();
        this.connections = new ArrayList<>();
        this.region = "";
    }

    public String getType() {
        return "Location";
    }

    public String getSummary() {
        if (region == null || region.isBlank()) {
            return "Type: " + locationType + " | No regions set";
        }
        return "Type: " + locationType + " | Region: " + region;
    }

    public void addSubLocation(String location) {
        subLocations.add(location);
    }

    public boolean removeSubLocation(String location) {
        return subLocations.remove(location);
    }

    public void addConnection(String connection) {
        connections.add(connection);
    }

    public boolean removeConnection(String connection) {
        return connections.remove(connection);
    }

    //Getters
    public String getLocationType() {
        return locationType;
    }

    public String getRegion() {
        return region;
    }

    public List<String> getSubLocations() {
        return subLocations;
    }

    public List<String> getConnections() {
        return connections;
    }

    //Setters
    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String toString() {
        return String.format("[Location] %s | Type: %s | Region: %s | Sub-locations: %s",
                getName(), locationType,
                region.isBlank() ? "none" : region,
                subLocations.isEmpty() ? "none" : String.join(", ", subLocations));
    }
}