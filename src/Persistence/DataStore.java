package Persistence;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import Model.*;

public class DataStore {
    private FileManager fileManager;

    public DataStore() throws IOException {
        this.fileManager = new FileManager();
    }

    public void saveWorld(World world) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        try(ObjectOutputStream outStream = new ObjectOutputStream(byteOut)){
            outStream.writeObject(world);
        }
        String filePath = fileManager.getWorldFilePath(world.getID());
        fileManager.writeFile(filePath, byteOut.toByteArray());
    }

    public World loadWorld(String worldId) throws IOException, ClassNotFoundException {
        String filePath = fileManager.getWorldFilePath(worldId);

        if (!fileManager.fileExists(filePath)) {
            throw new IOException("No save file found for world ID: " + worldId);
        }

        byte[] data = fileManager.readFile(filePath);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
        try (ObjectInputStream objIn = new ObjectInputStream(byteIn)) {
            return (World) objIn.readObject();
        }
    }

    public List<World> loadAllWorlds() throws IOException {
        List<World> loadedWorlds = new ArrayList<>();
        String[] filePaths = fileManager.getAllWorldFilePaths();

        for(String filePath : filePaths){
            String fileName = new File(filePath).getName();
            String worldId = fileName.replace(".ser", "");

            try {
                World world = loadWorld(worldId);
                loadedWorlds.add(world);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Warning: Could not load world file: "
                        + filePath + " — " + e.getMessage());
            }
        }
        return loadedWorlds;
    }

    public boolean deleteSavedWorld(String worldID) throws IOException{
        String filePath = fileManager.getWorldFilePath(worldID);
        return fileManager.deleteFile(filePath);
    }

    public boolean savedWorldExists(String worldID) throws IOException {
        String filePath = fileManager.getWorldFilePath(worldID);
        return fileManager.fileExists(filePath);
    }
}
