package Model;
import java.util.ArrayList;
import java.util.List;

public class Faction extends CodexEntry {
    private static final long serialVersionUID = 1L;

    private String goal;
    private String ideology;
    private List<String> members;
    private List<String> factionRelationships;

    public Faction(String id, String name, String description, String goal){
        super(id, name, description);

        this.goal = goal;
        this.ideology = "";
        this.members = new ArrayList<>();
        this.factionRelationships = new ArrayList<>();
    }

    public String getType(){
        return "Faction";
    }

    public String getSummary(){
        String memberCount = members.size() + " member" + (members.size() == 1 ? "" : "s");
        String shortGoal = goal.length() > 40 ? goal.substring(0, 40) + "..." : goal;
        return memberCount + " | Goal: " + shortGoal;
    }

    public void addMember(String memberName){
        members.add(memberName);
    }

    public boolean removeMember(String memberName){
        return members.remove(memberName);
    }

    public void addFactionRelationship(String relationship){
        factionRelationships.add(relationship);
    }

    public boolean removeFactionRelationship(String relationship){
        return factionRelationships.remove(relationship);
    }

    //Getters
    public String getGoal(){
        return goal;
    }

    public String getIdeology(){
        return ideology;
    }

    public List<String> getMembers(){
        return new ArrayList<>(members);
    }

    public List<String> getFactionRelationships(){
        return new ArrayList<>(factionRelationships);
    }

    //Setters
    public void setGoal(String goal){
        this.goal = goal;
    }

    public void setIdeology(String ideology){
        this.ideology = ideology;
    }

    public String toString() {
        return String.format("[Faction] %s | Members: %d | Goal: %s | Ideology: %s",
                getName(), members.size(),
                goal.isBlank() ? "none" : goal,
                ideology.isBlank() ? "none" : ideology);
    }

    @Override
    public CodexEntry deepCopy() {
        Faction copy = new Faction(getID(), getName(), getDescription(), goal);
        copy.setIdeology(ideology);
        members.forEach(copy::addMember);
        factionRelationships.forEach(copy::addFactionRelationship);
        return copy;
    }
}