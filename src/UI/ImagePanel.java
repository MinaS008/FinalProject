package UI;

import Helper.ThemeConstants;
import Persistence.ImageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImagePanel extends JPanel {
    public enum Mode {Detail, Editor}

    private static final int detailMaxW = 400;
    private static final int detailMahH = 280;
    private static final int editorMaxW = 240;
    private static final int editorMaxH = 160;

    private final ImageManager imageManager;
    private final Mode mode;

    private String currentEntryID;
    private BufferedImage cachedImage; //scaled preview, null when no image

    private final ImageCanvas previewCanvas;
    private final JButton uploadButton;
    private final JButton removeButton;
    private final JLabel statusLabel;

    public ImagePanel(ImageManager imageManager, Mode mode){
        this.imageManager = imageManager;
        this.mode = mode;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        previewCanvas = new ImageCanvas();
        previewCanvas.setAlignmentX(Component.LEFT_ALIGNMENT);

        uploadButton = buildButton("📷  Upload Image",
                ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover,
                ThemeConstants.colorTextSecondary);
        uploadButton.addActionListener(e->handleUpload());

        removeButton = buildButton("✕  Remove Image",
                ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover,
                ThemeConstants.colorDanger);
        removeButton.setVisible(false);
        removeButton.addActionListener(e -> handleRemove());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.add(uploadButton);
        buttonRow.add(removeButton);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeConstants.fontSmall);
        statusLabel.setForeground(ThemeConstants.colorTextPlaceholder);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(previewCanvas);
        add(spacer(8));
        add(buttonRow);
        add(spacer(4));
        add(statusLabel);

        ThemeManager.getInstance().addChangeListener(this::applyThemeColors);
    }

    //Public API
    public void load(String entryID){
        this.currentEntryID = entryID;
        statusLabel.setText(" ");

        if(entryID == null || !imageManager.hasImage(entryID)){
            cachedImage = null;
        } else {
            BufferedImage raw = imageManager.loadImage(entryID);
            cachedImage = raw == null ? null : ImageManager.scaledPreview(raw, maxW(), maxH());
        }

        updateControls();
        previewCanvas.revalidate();
        previewCanvas.repaint();
    }

    private void handleUpload(){
        if (currentEntryID == null) {
            statusLabel.setText("Save the entry first before adding an image.");
            statusLabel.setForeground(ThemeConstants.colorDanger);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose an Image");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files (png, jpg, gif, bmp, webp)",
                ImageManager.acceptedExtensions));
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File chosen = chooser.getSelectedFile();
        try {
            imageManager.saveImage(currentEntryID, chosen);

            // Reload and scale for display
            BufferedImage raw = imageManager.loadImage(currentEntryID);
            cachedImage = raw == null ? null
                    : ImageManager.scaledPreview(raw, maxW(), maxH());

            statusLabel.setText("Image saved: " + chosen.getName());
            statusLabel.setForeground(ThemeConstants.colorTextSecondary);
        } catch (IOException | IllegalArgumentException ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            statusLabel.setForeground(ThemeConstants.colorDanger);
        }

        updateControls();
        previewCanvas.revalidate();
        previewCanvas.repaint();
    }

    private void handleRemove(){
        if (currentEntryID == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this, "Remove this image from the entry?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        imageManager.deleteImage(currentEntryID);
        cachedImage = null;
        statusLabel.setText(" ");

        updateControls();
        previewCanvas.revalidate();
        previewCanvas.repaint();
    }

    //Internal helpers
    private void updateControls(){
        boolean hasImg = (cachedImage != null);
        removeButton.setVisible(hasImg);
        uploadButton.setText(hasImg ? "\uD83D\uDCF7  Replace Image": "📷  Upload Image");
    }

    private void applyThemeColors(){
        uploadButton.setBackground(ThemeConstants.colorSurface);
        removeButton.setBackground(ThemeConstants.colorSurface);
        statusLabel.setForeground(ThemeConstants.colorTextPlaceholder);
        repaint();
    }

    private int maxW(){return mode == Mode.Detail ? detailMaxW : editorMaxW;}
    private int maxH(){return mode == Mode.Detail ? detailMahH : editorMaxH;}

    private static Component spacer(int h){
        JPanel sp = new JPanel();
        sp.setOpaque(false);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        sp.setPreferredSize(new Dimension(0, h));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sp;
    }

    private JButton buildButton(String text, Color bg, Color hover, Color fg){
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(ThemeConstants.fontButton);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); btn.repaint(); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); btn.repaint(); }
        });
        return btn;
    }

    private class ImageCanvas extends JPanel {
        ImageCanvas() {
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        public Dimension getPreferredSize() {
            if (cachedImage != null){
                return new Dimension(cachedImage.getWidth() + 4, cachedImage.getHeight() + 4);
            }

            return new Dimension(maxW(), mode == Mode.Detail ? 160 : 100);
        }

        public Dimension getMaximumSize(){
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();
            int r = ThemeConstants.cornerRadius;

            if(cachedImage != null){
                g2.setColor(ThemeConstants.colorSurface);
                g2.fillRoundRect(0, 0, w, h, r, r);
                g2.setColor(ThemeConstants.colorBorder);
                g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);

                int imgW = cachedImage.getWidth();
                int imgH = cachedImage.getHeight();
                int x = (w-imgW)/2;
                int y = (h-imgH)/2;

                //Clip to rounded rect so corners don't bleed
                g2.setClip(new RoundRectangle2D.Float(
                        1, 1, w-2, h-2, r, r
                ));
                g2.drawImage(cachedImage, x, y, null);
                g2.setClip(null);
            } else {
                g2.setColor(ThemeConstants.colorSurface);
                g2.fillRoundRect(0, 0, w, h, r, r);

                // Dashed border
                float[] dash = {6f, 4f};
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10f, dash, 0f));
                g2.setColor(ThemeConstants.colorBorder);
                g2.drawRoundRect(1, 1, w - 3, h - 3, r, r);
                g2.setStroke(new BasicStroke());

                // Camera icon
                g2.setFont(new Font("SansSerif", Font.PLAIN, 28));
                g2.setColor(ThemeConstants.colorTextPlaceholder);
                FontMetrics fm = g2.getFontMetrics();
                String icon = "📷";
                int iconX = (w - fm.stringWidth(icon)) / 2;
                int iconY = h / 2 - 6;
                g2.drawString(icon, iconX, iconY);

                // Hint text
                g2.setFont(ThemeConstants.fontSmall);
                g2.setColor(ThemeConstants.colorTextPlaceholder);
                FontMetrics fm2 = g2.getFontMetrics();
                String hint = "No image attached";
                g2.drawString(hint, (w - fm2.stringWidth(hint)) / 2, iconY + 24);
            }
            g2.dispose();
        }
    }
}