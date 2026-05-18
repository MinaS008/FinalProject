package Model;
import java.util.ArrayList;
import java.util.List;


public class Item extends CodexEntry{
    private static final long serialVersionUID = 1L;

    public static final String rarityCommon = "Common";
    public static final String rarityUncommon = "Uncommon";
    public static final String rarityRare = "Rare";
    public static final String rarityEpic = "Epic";
    public static final String rarityLegendary = "Legendary";

    private String rarity;
    private String itemType;
    private String power;
    private List<String> ownerHistory;

    //Constructor
    public Item(String id, String name, String description, String rarity, String itemType){
        super(id, name, description);
        this.rarity = rarity;
        this.itemType = itemType;

        this.power = "";
        this.ownerHistory = new ArrayList<>();
    }

    public String getType(){
        return "Item";
    }

    public String getSummary(){
        String currentOwner = ownerHistory.isEmpty()
                ? "No owner"
                : "Current owner: " + ownerHistory.get(ownerHistory.size() - 1);
        return itemType + " | " + rarity + " | " + currentOwner;
    }

    public void addOwner(String ownerName){
        ownerHistory.add(ownerName);
    }

    public String getCurrentOwner(){
        if(ownerHistory.isEmpty()) return "Unknown";
        return ownerHistory.get(ownerHistory.size() - 1);
    }

    public List<String> getOwnerHistory(){
        return new ArrayList<>(ownerHistory);
    }

    //Getters
    public String getRarity(){
        return rarity;
    }

    public String getItemType(){
        return itemType;
    }

    public String getPower(){
        return power;
    }

    //Setters
    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String toString() {
        return String.format("[Item] %s | %s | %s | Power: %s | Current owner: %s",
                getName(), itemType, rarity,
                power.isBlank() ? "none" : power,
                getCurrentOwner());
    }

}
