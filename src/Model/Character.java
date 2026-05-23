package Model;
import java.util.ArrayList;
import java.util.List;


public class Character extends CodexEntry {
    private static final long serialVersionUID = 1L;

    private String role;
    private List<String> affiliations;
    private List<String> relationships;
    private List<String> abilities;
    private String backstory;

    public Character(String id, String name, String description, String role){
        super(id, name, description);
        this.role = role;
        this.backstory = "";
        this.affiliations = new ArrayList<>();
        this.relationships = new ArrayList<>();
        this.abilities = new ArrayList<>();
    }

    public String getType(){
        return "Character";
    }

    public String getSummary(){
        if(affiliations.isEmpty()){
            return "Role: " + role + " | No affiliations";
        }
        return "Role: " + role + " | Affiliations: " + String.join(", ", affiliations);
    }

    public void addAffiliation(String factionName){
        affiliations.add(factionName);
    }

    public boolean removeAffiliation(String factionName){
        return affiliations.remove(factionName);
    }

    public void addRelationship(String relationship){
        relationships.add(relationship);
    }

    public boolean removeRelationship(String relationship){
        return relationships.remove(relationship);
    }

    public void addAbility(String ability){
        abilities.add(ability);
    }

    public boolean removeAbility(String ability){
        return abilities.remove(ability);
    }

    //Getters
    public String getRole(){
        return role;
    }

    public List<String> getRelationships(){
        return new ArrayList<>(relationships);
    }

    public List<String> getAffiliations(){
        return new ArrayList<>(affiliations);
    }

    public List<String> getAbilities(){
        return new ArrayList<>(abilities);
    }

    public String getBackstory(){
        return backstory;
    }

    //Setters
    public void setRole(String role){
        this.role = role;
    }

    public void setBackstory(String backstory){
        this.backstory = backstory;
    }

    public String toString() {
        return String.format("[Character] %s | Role: %s | Abilities: %s | %s",
                getName(), role,
                abilities.isEmpty() ? "none" : String.join(", ", abilities),
                getDescription());
    }

    @Override
    public CodexEntry deepCopy() {
        Character copy = new Character(getID(), getName(), getDescription(), role);
        copy.setBackstory(backstory);
        affiliations.forEach(copy::addAffiliation);
        abilities.forEach(copy::addAbility);
        relationships.forEach(copy::addRelationship);
        return copy;
    }
}