package Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TimelineEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String id;
    private String title;
    private String description;
    private String era;
    private String dateLabel; //in-world date/year as display text
    private int sortOrder; //numeric ordering (ascending = earlier)

    private List<String> involvedEntities;
    private List<String> consequences;

    private final String createdAt;
    private String updatedAt;

    public TimelineEvent(String id, String title, String description, String era, String dateLabel, int sortOrder){
        this.id = id;
        this.title = title;
        this.description = description;
        this.era = era;
        this.dateLabel = dateLabel;
        this.sortOrder = sortOrder;

        this.involvedEntities = new ArrayList<>();
        this.consequences = new ArrayList<>();

        String now = LocalDateTime.now().format(FORMATTER);
        this.createdAt = now;
        this.updatedAt = now;
    }

    //List mutators
    public void addInvolvedEntity(String name){
        if(name != null && !name.isBlank()) involvedEntities.add(name);
    }

    public boolean removeInvolvedEntity(String name){
        return involvedEntities.remove(name);
    }

    public void addConsequence(String consequence){
        if (consequence != null && !consequence.isBlank()) consequences.add(consequence);
    }

    public boolean removeConsequence(String consequence){
        return consequences.remove(consequence);
    }

    //Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription()  { return description; }
    public String getEra() { return era; }
    public String getDateLabel() { return dateLabel; }
    public int    getSortOrder() { return sortOrder; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public List<String> getInvolvedEntities() { return new ArrayList<>(involvedEntities); }
    public List<String> getConsequences() { return new ArrayList<>(consequences); }

    //Setters
    public void setTitle(String title) {
        this.title = title;
        touch();
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public void setEra(String era) {
        this.era = era;
        touch();
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
        touch();
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        touch();
    }

    //Helpers
    private void touch(){
        this.updatedAt = LocalDateTime.now().format(FORMATTER);
    }

    public String toString(){
        return String.format("[TimelineEvent] #%d %s (%s) | %s",
                sortOrder, title, era, dateLabel);
    }

    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof TimelineEvent other)) return false;
        return this.id.equals(other.id);
    }

    public int hashCode() {return id.hashCode();}
}