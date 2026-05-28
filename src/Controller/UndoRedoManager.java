package Controller;

import Model.CodexEntry;
import Model.World;

import java.util.Stack;

public class UndoRedoManager {
    public enum ActionType {ADD, DELETE, EDIT}

    public static class Action {
        private final ActionType type;
        private final String worldID;
        private final CodexEntry snapshot; //full entry state at the time of recording

        public Action(ActionType type, String worldID, CodexEntry snapshot){
            this.type = type;
            this.worldID = worldID;
            this.snapshot = snapshot;
        }

        public ActionType getType(){return type;}
        public String getWorldID(){return worldID;}
        public CodexEntry getSnapshot(){return snapshot;}
    }

    private static final int maxHistory = 50;
    private final Stack<Action> undoStack = new Stack<>();
    private final Stack<Action> redoStack = new Stack<>();

    public void recordAdd(World world, CodexEntry entry){
        push(new Action(ActionType.ADD, world.getID(), entry.deepCopy()));
    }

    public void recordDelete(World world, CodexEntry entry){
        push(new Action(ActionType.DELETE, world.getID(), entry));
    }

    public void recordEdit(World world, CodexEntry beforeSnapshot){
        push(new Action(ActionType.EDIT, world.getID(), beforeSnapshot));
    }

    //Undo-redo
    public String undo(World world){
        if(undoStack.isEmpty()) return null;

        Action action = undoStack.pop();
        if(!action.getWorldID().equals(world.getID())){
            //Safety: if the world switched without clearing stacks, skip
            undoStack.push(action);
            return null;
        }

        String description = applyUndo(world, action);
        redoStack.push(action);
        return description;
    }

    public String redo(World world){
        if(redoStack.isEmpty()) return null;

        Action action = redoStack.pop();
        if(!action.getWorldID().equals(world.getID())){
            redoStack.push(action);
            return null;
        }

        String description = applyRedo(world, action);
        undoStack.push(action);
        return description;
    }

    public boolean canUndo() {return !undoStack.isEmpty();}
    public boolean canRedo() {return !redoStack.isEmpty();}

    public void clearHistory(){
        undoStack.clear();
        redoStack.clear();
    }

    public int getUndoCount() {return undoStack.size();}
    public int getRedoCount() {return redoStack.size();}

    public String getUndoLabel() {
        if (undoStack.isEmpty()) return "Nothing to undo";
        Action a = undoStack.peek();
        return "Undo " + verbFor(a.getType()) + " \"" + a.getSnapshot().getName() + "\"";
    }

    public String getRedoLabel() {
        if (redoStack.isEmpty()) return "Nothing to redo";
        Action a = redoStack.peek();
        return "Redo " + verbFor(a.getType()) + " \"" + a.getSnapshot().getName() + "\"";
    }

    //Private helpers
    private void push(Action action){
        redoStack.clear();
        undoStack.push(action);
        if(undoStack.size() > maxHistory){
            undoStack.remove(0);
        }
    }

    private String applyUndo(World world, Action action){
        CodexEntry snapshot = action.getSnapshot();
        switch (action.getType()) {
            case ADD:
                // Undo an add - delete the entry
                world.removeEntry(snapshot.getID());
                return "Undid add of \"" + snapshot.getName() + "\"";

            case DELETE:
                // Undo a delete - re-add the entry
                world.addEntry(snapshot);
                return "Restored \"" + snapshot.getName() + "\"";

            case EDIT:
                // Undo an edit - restore all fields from the snapshot
                restoreEntry(world, snapshot);
                return "Undid edit of \"" + snapshot.getName() + "\"";

            default:
                return "Undo performed";
        }
    }

    private String applyRedo(World world, Action action){
        CodexEntry snapshot = action.getSnapshot();
        switch (action.getType()) {
            case ADD:
                world.addEntry(snapshot);
                return "Redid add of \"" + snapshot.getName() + "\"";

            case DELETE:
                world.removeEntry(snapshot.getID());
                return "Re-deleted \"" + snapshot.getName() + "\"";

            case EDIT:
                restoreEntry(world, snapshot);
                return "Redid edit of \"" + snapshot.getName() + "\"";

            default:
                return "Redo performed";
        }
    }

    private void restoreEntry(World world, CodexEntry snapshot){
        CodexEntry live = world.getEntryByID(snapshot.getID());
        if(live == null){
            world.addEntry(snapshot);
            return;
        }

        //Restore base fields
        live.setName(snapshot.getName());
        live.setDescription(snapshot.getDescription());

        copySubclassFields(snapshot, live);
    }

    private void copySubclassFields(CodexEntry src, CodexEntry dst) {
        if (src instanceof Model.Character && dst instanceof Model.Character) {
            Model.Character s = (Model.Character) src;
            Model.Character d = (Model.Character) dst;
            d.setRole(s.getRole());
            d.setBackstory(s.getBackstory());
            // Replace list contents
            new java.util.ArrayList<>(d.getAffiliations()).forEach(d::removeAffiliation);
            s.getAffiliations().forEach(d::addAffiliation);
            new java.util.ArrayList<>(d.getAbilities()).forEach(d::removeAbility);
            s.getAbilities().forEach(d::addAbility);
            new java.util.ArrayList<>(d.getRelationships()).forEach(d::removeRelationship);
            s.getRelationships().forEach(d::addRelationship);
        } else if (src instanceof Model.Location && dst instanceof Model.Location) {
            Model.Location s = (Model.Location) src;
            Model.Location d = (Model.Location) dst;
            d.setLocationType(s.getLocationType());
            d.setRegion(s.getRegion());
            new java.util.ArrayList<>(d.getSubLocations()).forEach(d::removeSubLocation);
            s.getSubLocations().forEach(d::addSubLocation);
            new java.util.ArrayList<>(d.getConnections()).forEach(d::removeConnection);
            s.getConnections().forEach(d::addConnection);
        } else if (src instanceof Model.Item && dst instanceof Model.Item) {
            Model.Item s = (Model.Item) src;
            Model.Item d = (Model.Item) dst;
            d.setItemType(s.getItemType());
            d.setRarity(s.getRarity());
            d.setPower(s.getPower());
            // Rebuild owner history
            new java.util.ArrayList<>(d.getOwnerHistory()).forEach(o -> {});  // no removeOwner API
            // Use direct list replacement via the model's internal list
            d.replaceOwnerHistory(s.getOwnerHistory());
        } else if (src instanceof Model.Faction && dst instanceof Model.Faction) {
            Model.Faction s = (Model.Faction) src;
            Model.Faction d = (Model.Faction) dst;
            d.setGoal(s.getGoal());
            d.setIdeology(s.getIdeology());
            new java.util.ArrayList<>(d.getMembers()).forEach(d::removeMember);
            s.getMembers().forEach(d::addMember);
            new java.util.ArrayList<>(d.getFactionRelationships()).forEach(d::removeFactionRelationship);
            s.getFactionRelationships().forEach(d::addFactionRelationship);
        } else if (src instanceof Model.LoreEntry && dst instanceof Model.LoreEntry) {
            Model.LoreEntry s = (Model.LoreEntry) src;
            Model.LoreEntry d = (Model.LoreEntry) dst;
            d.setEra(s.getEra());
            new java.util.ArrayList<>(d.getTimeline()).forEach(d::removeTimelineEntry);
            s.getTimeline().forEach(d::addTimelineEntry);
            new java.util.ArrayList<>(d.getConsequences()).forEach(d::removeConsequence);
            s.getConsequences().forEach(d::addConsequence);
            new java.util.ArrayList<>(d.getReferences()).forEach(d::removeReference);
            s.getReferences().forEach(d::addReference);
        }
    }

    private String verbFor(ActionType type){
        switch(type){
            case ADD: return "Add";
            case DELETE: return "Delete";
            case EDIT: return "Edit";
            default: return "Action";
        }
    }
}