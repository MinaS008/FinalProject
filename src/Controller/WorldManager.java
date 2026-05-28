package Controller;
import Utilities.*;
import Model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WorldManager {
    private Map<String, World> worlds;
    private IDGenerator idGenerator;

    public WorldManager(){
        this.worlds = new HashMap<>();
        this.idGenerator = new IDGenerator();
    }

    public World createWorld(String name, String description){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("World name cannot be empty.");
        }

        String id = idGenerator.generatedID();
        World newWorld = new World(id, name, description);
        worlds.put(id, newWorld);
        return newWorld;
    }

    public World getWorldByID(String worldID){
        return worlds.get(worldID);
    }

    public List<World> getAllWorlds(){
        return new ArrayList<>(worlds.values());
    }

    public void updateWorld(String worldID, String newName,String newDescription){
        World world = getExistingWorld(worldID);

        if(newName == null || newName.isBlank()){
            throw new IllegalArgumentException("World name cannot be empty.");
        }

        world.setName(newName);
        world.setDescription(newDescription);
    }

    public boolean deleteWorld(String worldID){
        if(worlds.containsKey(worldID)){
            worlds.remove(worldID);
            return true;
        }
        return false;
    }

    //Entry CRUD
    public void addEntry(String worldID, CodexEntry entry){
        World world = getExistingWorld(worldID);

        if(entry == null){
            throw new IllegalArgumentException("Cannot add a null entry.");
        }

        world.addEntry(entry);
    }

    public CodexEntry getEntryByID(String worldID, String entryID){
        World world = getExistingWorld(worldID);
        return world.getEntryByID(entryID);
    }

    public List<CodexEntry> getEntries(String worldID, String type){
        World world = getExistingWorld(worldID);

        if(type == null){
            return world.getEntries();
        }

        return world.getEntriesByType(type);
    }

    public void updateEntry(String worldID, String entryID, String newName, String newDescription) {
        World world = getExistingWorld(worldID);
        CodexEntry entry = world.getEntryByID(entryID);

        if (entry == null) {
            throw new IllegalArgumentException(
                    "Entry with ID " + entryID + " not found in world " + worldID);
        }
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Entry name cannot be empty.");
        }

        entry.setName(newName);
        entry.setDescription(newDescription);
    }

    public boolean deleteEntry(String worldID, String entryID){
        World world = getExistingWorld(worldID);
        return world.removeEntry(entryID);
    }

    public String generateNewID(){
        return idGenerator.generatedID();
    }

    public void registerLoadedID(String id){
        idGenerator.registerExistingID(id);
    }

    public void loadWorld(World world) {
        worlds.put(world.getID(), world);
    }

    public RelationshipGraph getGraph(String worldID) {
        return getExistingWorld(worldID).getRelationshipGraph();
    }

    public void addLink(String worldID, String fromID, String toID, String label, String type) {
        getExistingWorld(worldID).getRelationshipGraph().addLink(fromID, toID, label, type);
    }

    public boolean removeLink(String worldID, String fromID, String toID, String label) {
        return getExistingWorld(worldID).getRelationshipGraph().removeLink(fromID, toID, label);
    }

    public List<RelationshipGraph.RelationshipLink> getLinks(String worldID, String entryID) {
        return getExistingWorld(worldID).getRelationshipGraph().getConnections(entryID);
    }

    //Private Helpers
    private World getExistingWorld(String worldID){
        World world = worlds.get(worldID);
        if (world == null) {
            throw new IllegalArgumentException("No world found with ID: " + worldID);
        }
        return world;
    }

}