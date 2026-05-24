package UI;

import Helper.ThemeConstants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    private static ThemeManager instance;
    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    private Theme currentTheme = Theme.DARK;
    private final List<Runnable> changeListeners = new ArrayList<>();

    private ThemeManager() {}

    public enum Theme {
        DARK   ("Dark Codex",    "✦ The default — deep navy with gold accents"),
        LIGHT  ("Parchment",     "☀ Warm parchment tones for daylight sessions"),
        ARCANE ("Arcane Night",  "✧ Deep violet with arcane purple accents");

        public final String displayName;
        public final String description;

        Theme(String displayName, String description) {
            this.displayName  = displayName;
            this.description  = description;
        }
    }

    public Theme getCurrentTheme() { return currentTheme; }

    public void applyTheme(Theme theme, Window rootWindow) {
        if (theme == null) return;
        currentTheme = theme;
        loadPalette(theme);
        if (rootWindow != null) {
            repaintTree(rootWindow);
        }
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null) changeListeners.add(listener);
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    public void showThemePicker(Component parent, Window rootWindow) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(parent),
                "Choose Theme",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setSize(420, 320);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(ThemeConstants.colorBackground);
        dialog.getContentPane().setLayout(new BorderLayout(0, 0));

        JLabel header = new JLabel("  Choose Your Theme");
        header.setFont(ThemeConstants.fontHeading);
        header.setForeground(ThemeConstants.colorAccent);
        header.setOpaque(true);
        header.setBackground(ThemeConstants.colorSurface);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.colorBorder),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        dialog.getContentPane().add(header, BorderLayout.NORTH);

        ButtonGroup group   = new ButtonGroup();
        JPanel      options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setBackground(ThemeConstants.colorBackground);
        options.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        JRadioButton[] buttons = new JRadioButton[Theme.values().length];
        for (int i = 0; i < Theme.values().length; i++) {
            Theme t = Theme.values()[i];
            JRadioButton rb = buildThemeRadioButton(t);
            rb.setSelected(t == currentTheme);
            group.add(rb);
            options.add(rb);
            options.add(Box.createVerticalStrut(8));
            buttons[i] = rb;
        }
        dialog.getContentPane().add(options, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(ThemeConstants.colorSurface);
        footer.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, ThemeConstants.colorBorder));

        JButton cancel = buildButton("Cancel",
                ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover,
                ThemeConstants.colorTextSecondary);
        JButton apply  = buildButton("Apply Theme",
                ThemeConstants.colorAccent, ThemeConstants.colorAccentDark,
                ThemeConstants.colorBackground);

        cancel.addActionListener(e -> dialog.dispose());

        apply.addActionListener(e -> {
            for (int i = 0; i < Theme.values().length; i++) {
                if (buttons[i].isSelected()) {
                    applyTheme(Theme.values()[i], rootWindow);
                    break;
                }
            }
            dialog.dispose();
        });

        footer.add(cancel);
        footer.add(apply);
        dialog.getContentPane().add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private static void loadPalette(Theme theme) {
        switch (theme) {

            case DARK:
                // Original deep-navy palette
                ThemeConstants.colorBackground      = new Color(15,  20,  35);
                ThemeConstants.colorSurface         = new Color(24,  32,  52);
                ThemeConstants.colorSurfaceHover    = new Color(35,  46,  72);
                ThemeConstants.colorBorder          = new Color(50,  65,  95);

                ThemeConstants.colorTextPrimary     = new Color(230, 220, 200);
                ThemeConstants.colorTextSecondary   = new Color(140, 130, 115);
                ThemeConstants.colorTextPlaceholder = new Color(90,  85,  75);

                ThemeConstants.colorAccent          = new Color(196, 160, 80);
                ThemeConstants.colorAccentDark      = new Color(155, 125, 55);
                ThemeConstants.colorDanger          = new Color(180, 65,  65);
                ThemeConstants.colorDangerDark      = new Color(140, 45,  45);
                break;

            case LIGHT:
                // Warm parchment palette
                ThemeConstants.colorBackground      = new Color(245, 238, 220);
                ThemeConstants.colorSurface         = new Color(235, 226, 206);
                ThemeConstants.colorSurfaceHover    = new Color(222, 211, 188);
                ThemeConstants.colorBorder          = new Color(185, 170, 140);

                ThemeConstants.colorTextPrimary     = new Color(45,  35,  20);
                ThemeConstants.colorTextSecondary   = new Color(100, 85,  60);
                ThemeConstants.colorTextPlaceholder = new Color(160, 145, 115);

                ThemeConstants.colorAccent          = new Color(140, 90,  20);
                ThemeConstants.colorAccentDark      = new Color(105, 65,  10);
                ThemeConstants.colorDanger          = new Color(160, 50,  50);
                ThemeConstants.colorDangerDark      = new Color(120, 30,  30);
                break;

            case ARCANE:
                // Deep violet / purple-teal arcane palette
                ThemeConstants.colorBackground      = new Color(12,  8,   28);
                ThemeConstants.colorSurface         = new Color(22,  16,  46);
                ThemeConstants.colorSurfaceHover    = new Color(38,  28,  72);
                ThemeConstants.colorBorder          = new Color(70,  50,  110);

                ThemeConstants.colorTextPrimary     = new Color(220, 210, 240);
                ThemeConstants.colorTextSecondary   = new Color(145, 130, 175);
                ThemeConstants.colorTextPlaceholder = new Color(85,  75,  105);

                ThemeConstants.colorAccent          = new Color(170, 100, 230);
                ThemeConstants.colorAccentDark      = new Color(130, 70,  185);
                ThemeConstants.colorDanger          = new Color(200, 60,  100);
                ThemeConstants.colorDangerDark      = new Color(155, 40,  75);
                break;
        }
    }

    private static void repaintTree(Component c) {
        c.repaint();
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                repaintTree(child);
            }
        }
        if (c instanceof JComponent) {
            ((JComponent) c).revalidate();
        }
    }

    private static JRadioButton buildThemeRadioButton(Theme t) {
        // Two-line HTML label: name (bold) + description (small, dimmer)
        String html = "<html><b>" + t.displayName + "</b><br>"
                + "<span style='font-size:10px;color:#888'>" + t.description + "</span></html>";
        JRadioButton rb = new JRadioButton(html);
        rb.setFont(ThemeConstants.fontBody);
        rb.setForeground(ThemeConstants.colorTextPrimary);
        rb.setBackground(ThemeConstants.colorBackground);
        rb.setOpaque(true);
        rb.setFocusPainted(false);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return rb;
    }

    private static JButton buildButton(String text,
                                       Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(ThemeConstants.fontButton);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 34));
        return btn;
    }
}