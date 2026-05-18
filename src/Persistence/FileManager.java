package Persistence;
import java.io.*;
import java.nio.file.*;


public class FileManager {
    private static final String rootDir = "nexus_data";
    private static final String worldsDir = rootDir + File.separator + " worlds.";
    private static final String fileExtension = " .ser";

    public FileManager() throws IOException {
        createDirectoriesIfAbsent();
    }

    private void createDirectoriesIfAbsent() throws IOException {
        Files.createDirectories(Paths.get(worldsDir));
    }

    public void writeFile(String filePath, byte[] data) throws IOException {
        Files.write(Paths.get(filePath), data);
    }

    public byte[] readFile(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    public boolean deleteFile(String filePath) throws IOException {
        return Files.deleteIfExists(Paths.get(filePath));
    }

    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public String getWorldFilePath(String worldId) {
        return worldsDir + File.separator + worldId + fileExtension;
    }

    public String[] getAllWorldFilePaths() throws IOException {
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(worldsDir), "*" + fileExtension)){
            java.util.List<String> paths = new java.util.ArrayList<>();
            for (Path entry : stream) {
                paths.add(entry.toString());
            }
            return paths.toArray(new String[0]);
        }
    }


}
