package Persistence;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.UUID;

public class ImageManager {
    private static final String imagesDir = "nexus_data" + File.separator + "images";
    private static final String manifestPath = imagesDir + File.separator + "manifest.properties";

    public static final String[] acceptedExtensions = {"png", "jpg", "jpeg", "gif", "bmp", "webp"};
    private static final int maxSavedDimension = 1200;

    private final Properties manifest;

    public ImageManager() throws IOException {
        Files.createDirectories(Paths.get(imagesDir));
        manifest = new Properties();
        File mf = new File(manifestPath);
        if (mf.exists()) {
            try (InputStream in = new FileInputStream(mf)) {
                manifest.load(in);
            }
        }
    }

    public boolean hasImage(String entryID){
        return manifest.containsKey(entryID);
    }

    public BufferedImage loadImage(String entryID){
        String fileName = manifest.getProperty(entryID);
        if(fileName == null) return null;

        File file = new File(imagesDir + File.separator + fileName);
        if(!file.exists()){
            manifest.remove(entryID);
            persistManifest();
            return null;
        }

        try {
            return ImageIO.read(file);
        } catch (IOException e){
            System.err.println("[ImageManager] Could not read image for entry " + entryID + ": " + e.getMessage());
            return null;
        }
    }

    public void saveImage(String entryID, File sourceFile) throws IOException {
        String ext = extension(sourceFile.getName());
        if (!isAcceptedExtension(ext)) {
            throw new IllegalArgumentException(
                    "Unsupported image format: " + ext
                            + ". Supported: png, jpg, jpeg, gif, bmp, webp");
        }

        deleteImage(entryID);

        BufferedImage original = ImageIO.read(sourceFile);
        if(original == null){
            throw new IOException("Could not decode image file: " + sourceFile.getName());
        }
        BufferedImage toSave = downscaleIfNeeded(original);

        String fileName = entryID + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
        File dest = new File(imagesDir + File.separator + fileName);

        String writerFormat = ext.equals("jpg") || ext.equals("jpeg") ? "jpg" : "png";

        if (writerFormat.equals("jpg") && toSave.getColorModel().hasAlpha()) {
            BufferedImage rgb = new BufferedImage(
                    toSave.getWidth(), toSave.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(toSave, 0, 0, null);
            g.dispose();
            toSave = rgb;
        }
        ImageIO.write(toSave, writerFormat, dest);

        manifest.setProperty(entryID, fileName);
        persistManifest();
    }

    public void deleteImage(String entryId){
        String filename = manifest.getProperty(entryId);
        if (filename == null) return;

        File file = new File(imagesDir + File.separator + filename);
        if (file.exists()) {
            file.delete();
        }

        manifest.remove(entryId);
        persistManifest();
    }

    public void deleteImagesForWorld(Iterable<String> entryIDs){
        for(String id: entryIDs){
            deleteImage(id);
        }
    }

    public static BufferedImage scaledPreview(BufferedImage source,
                                              int maxWidth, int maxHeight) {
        if (source == null) return null;

        int srcW = source.getWidth();
        int srcH = source.getHeight();

        if (srcW <= maxWidth && srcH <= maxHeight) return source;

        double scale = Math.min((double) maxWidth / srcW, (double) maxHeight / srcH);
        int newW = Math.max(1, (int) (srcW * scale));
        int newH = Math.max(1, (int) (srcH * scale));

        BufferedImage result = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(source, 0, 0, newW, newH, null);
        g2.dispose();
        return result;
    }

    //Private helpers
    private void persistManifest(){
        try(OutputStream out = new FileOutputStream(manifestPath)){
            manifest.store(out, "Nexus Codex - Image Manifest");
        } catch (IOException e){
            System.err.println("[ImageManager] Failed to persist manifest: " + e.getMessage());
        }
    }

    private static String extension(String fileName){
        int dot = fileName.lastIndexOf('.');
        return (dot >= 0 && dot < fileName.length() - 1)
                ? fileName.substring(dot + 1).toLowerCase()
                : "";
    }

    private static boolean isAcceptedExtension(String ext){
        for(String accepted : acceptedExtensions){
            if(accepted.equalsIgnoreCase(ext)) return true;
        }
        return false;
    }

    private static BufferedImage downscaleIfNeeded(BufferedImage img){
        return scaledPreview(img, maxSavedDimension, maxSavedDimension);
    }
}

