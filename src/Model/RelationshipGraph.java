package Model;
import java.io.*;
import java.util.*;

public class RelationshipGraph implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, List<RelationshipLink>> adjacency;

    public RelationshipGraph() {
        this.adjacency = new HashMap<>();
    }

    //Public API
    public void addLink(String fromID, String toID, String label, String type){
        if(fromID == null || toID == null || fromID.equals(toID)) return;

        RelationshipLink forward = new RelationshipLink(fromID, toID, label, type);
        RelationshipLink reverse = new RelationshipLink(toID, fromID, label, type);

        if(!hasLink(fromID, toID, label)){
            adjacency.computeIfAbsent(fromID, k -> new ArrayList<>()).add(forward);
            adjacency.computeIfAbsent(toID, k -> new ArrayList<>()).add(reverse);
        }
    }

    public boolean removeLink(String fromID, String toID, String label){
        boolean removed = false;

        List<RelationshipLink> fromList = adjacency.get(fromID);
        if(fromList != null){
            removed = fromList.removeIf(l -> l.getToID().equals(toID) && l.getLabel().equals(label));
        }

        List<RelationshipLink> toList = adjacency.get(toID);
        if(toList != null){
            toList.removeIf(l -> l.getToID().equals(fromID) && l.getLabel().equals(label));
        }
        return removed;
    }

    public void removeAllLinksFor(String entryID){
        List<RelationshipLink> links = adjacency.remove(entryID);

        for(RelationshipLink link : links){
            List<RelationshipLink> otherList = adjacency.get(link.getToID());
            if(otherList != null){
                otherList.removeIf(l -> l.getToID().equals(entryID));
            }
        }
    }

    public List<RelationshipLink> getConnections(String entryID) {
        List<RelationshipLink> list = adjacency.get(entryID);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public List<RelationshipLink> getAllLinks() {
        Set<String> seen = new HashSet<>();
        List<RelationshipLink> result = new ArrayList<>();

        for (List<RelationshipLink> links : adjacency.values()) {
            for (RelationshipLink link : links) {
                // Canonical key: smaller ID first, so A-B and B-A produce the same key
                String key = canonicalKey(link.getFromID(), link.getToID(), link.getLabel());
                if (seen.add(key)) {
                    result.add(link);
                }
            }
        }
        return result;
    }

    public boolean hasLink(String fromID, String toID, String label) {
        List<RelationshipLink> list = adjacency.get(fromID);
        if (list == null) return false;
        for (RelationshipLink l : list) {
            if (l.getToID().equals(toID) && l.getLabel().equals(label)) return true;
        }
        return false;
    }

    public int getLinkCount(){
        return getAllLinks().size();
    }

    public boolean isEmpty(){
        return adjacency.isEmpty();
    }

    //Helpers
    private String canonicalKey(String a, String b, String label) {
        if (a.compareTo(b) <= 0) return a + "|" + b + "|" + label;
        return b + "|" + a + "|" + label;
    }

    public static class RelationshipLink implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String fromID;
        private final String toID;
        private final String label;
        private final String type;

        public RelationshipLink(String fromID, String toID, String label, String type){
            this.fromID = fromID;
            this.toID = toID;
            this.label = label;
            this.type = type;
        }

        public String getFromID(){ return fromID;}
        public String getToID() {return toID;}
        public String getLabel(){return label;}
        public String getType() {return type;}

        public String toString() {
            return fromID + "--[" + label + "]-->" + toID;
        }
    }
}
