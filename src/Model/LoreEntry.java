package Model;
import java.util.ArrayList;
import java.util.List;


public class LoreEntry extends CodexEntry {
    private static final long serialVersionUID = 1L;

    public static final String eraAncient = "Ancient Era";
    public static final String eraClassical = "Classical Era";
    public static final String eraMedieval = "Medieval Era";
    public static final String eraModern = "Modern Era";
    public static final String eraFuture = "Future Era";
    public static final String eraMythic = "Mythic Era";
    public static final String eraUnknown = "Unknown Era";

    private List<String> timeline;
    private List<String> consequences;
    private List<String> references;
    private String era;

    public LoreEntry(String id, String name, String description, String era){
        super(id, name, description);
        this.era = era;

        this.timeline = new ArrayList<>();
        this.consequences = new ArrayList<>();
        this.references = new ArrayList<>();
    }

    public String getType(){
        return "Lore";
    }

    public String getSummary(){
        return era + " | "
                + timeline.size() + " timeline entr" + (timeline.size() == 1 ? "y" : "ies")
                + " | "
                + consequences.size() + " consequence" + (consequences.size() == 1 ? "" : "s");
    }

    public void addTimelineEntry(String timelineBeat){
        timeline.add(timelineBeat);
    }

    public boolean removeTimelineEntry(String timelineBeat){
        return timeline.remove(timelineBeat);
    }

    public void addConsequence(String consequence){
        consequences.add(consequence);
    }

    public boolean removeConsequence(String consequence){
        return consequences.remove(consequence);
    }

    public void addReference(String referenceName){
        references.add(referenceName);
    }

    public boolean removeReference(String referenceName){
        return references.remove(referenceName);
    }

    //Getters
    public String getEra(){
        return era;
    }

    public List<String> getTimeline(){
        return new ArrayList<>(timeline);
    }

    public List<String> getConsequences(){
        return new ArrayList<>(consequences);
    }

    public List<String> getReferences(){
        return new ArrayList<>(references);
    }

    //Setters
    public void setEra(String era){
        this.era = era;
    }

    public String toString(){
        return String.format("[Lore] %s | Era: %s | Timeline entries: %d | Consequences: %d",
                getName(), era, timeline.size(), consequences.size());
    }

    @Override
    public CodexEntry deepCopy() {
        LoreEntry copy = new LoreEntry(getID(), getName(), getDescription(), era);
        timeline.forEach(copy::addTimelineEntry);
        consequences.forEach(copy::addConsequence);
        references.forEach(copy::addReference);
        return copy;
    }
}