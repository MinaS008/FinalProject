package Model;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;



public class World implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String id;
    private String name;
    private String description;
    private final String createdAt;
    private String updatedAt;

    private List<CodexEntry> entries;
    private RelationshipGraph relationshipGraph;


    public World(String id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;

        String now = LocalDateTime.now().format(FORMATTER);
        this.createdAt = now;
        this.updatedAt = now;
        this.entries = new ArrayList<>();
        this.relationshipGraph = new RelationshipGraph();
    }

    public void addEntry(CodexEntry entry){
        if(entry == null){
            throw new IllegalArgumentException("Cannot add a null entry to a world.");
        }
        entries.add(entry);
        refreshUpdatedAt();
    }

    public boolean removeEntry(String entryID){
        boolean removed = entries.removeIf(e-> e.getID().equals(entryID));
        if (removed) {
            relationshipGraph.removeAllLinksFor(entryID);
            refreshUpdatedAt();
        }
        return removed;
    }

    public CodexEntry getEntryByID(String entryID){
        for(CodexEntry entry: entries){
            if(entry.getID().equals(entryID)){
                return entry;
            }
        }
        return null;
    }

    public List<CodexEntry> getEntries(){
        return new ArrayList<>(entries);
    }

    public List<CodexEntry> getEntriesByType(String type){
        List<CodexEntry> filtered = new ArrayList<>();
        for(CodexEntry entry : entries){
            if(entry.getType().equalsIgnoreCase(type)){
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public RelationshipGraph getRelationshipGraph(){
        if(relationshipGraph == null) relationshipGraph = new RelationshipGraph();
        return relationshipGraph;
    }

    public int getEntryCount(){
        return entries.size();
    }

    public String getID(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String getCreatedAt(){
        return createdAt;
    }

    public String getUpdatedAt(){
        return updatedAt;
    }

    public void setName(String name){
        this.name = name;
        refreshUpdatedAt();
    }


    public void setDescription(String description){
        this.description = description;
        refreshUpdatedAt();
    }

    //Helpers
    public void refreshUpdatedAt(){
        this.updatedAt = LocalDateTime.now().format(FORMATTER);
    }

    public String toString(){
        return String.format("World: %s (ID: %s) - %d entries | Created: %s", name, id, entries.size(), createdAt);
    }

    public boolean equals(Object object){
        if(this == object) return true;
        if(!(object instanceof World other)) return false;
        return this.id.equals(other.id);
    }

    public int hashCode(){
        return id.hashCode();
    }
}