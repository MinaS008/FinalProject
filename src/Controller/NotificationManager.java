package Controller;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import Helper.*;

public class NotificationManager {
    private static final int toastDurationMS = 2500;
    private static final int fadeStepsMS     = 50;
    private static final int fadeSteps       = 20;

    private NotificationManager() {}

    public static void showSuccess(Component parent, String message) {
        showToast(parent, message,
                new Color(45, 130, 75),
                new Color(35, 100, 55));
    }

    public static void showErrorToast(Component parent, String message) {
        showToast(parent, message,
                new Color(160, 50, 50),
                new Color(120, 35, 35));
    }

    public static void showWarning(Component parent, String message) {
        runOnEDT(() -> JOptionPane.showMessageDialog(
                resolveWindow(parent),
                styledMessagePanel(message, new Color(180, 130, 40)),
                "Validation Error",
                JOptionPane.WARNING_MESSAGE));
    }

    public static void showError(Component parent, String message) {
        runOnEDT(() -> JOptionPane.showMessageDialog(
                resolveWindow(parent),
                styledMessagePanel(message, ThemeConstants.colorDanger),
                "Error",
                JOptionPane.ERROR_MESSAGE));
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        // Must run on EDT and return the result — use invokeAndWait if not already on EDT.
        if (SwingUtilities.isEventDispatchThread()) {
            return doShowConfirm(parent, message, title);
        }
        final boolean[] result = {false};
        try {
            SwingUtilities.invokeAndWait(() ->
                    result[0] = doShowConfirm(parent, message, title));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result[0];
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private static boolean doShowConfirm(Component parent, String message, String title) {
        int choice = JOptionPane.showConfirmDialog(
                resolveWindow(parent),
                styledMessagePanel(message, ThemeConstants.colorTextPrimary),
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    private static void showToast(Component parent, String message, Color bgColor, Color borderColor) {
        // Always run toast creation and display on the EDT
        runOnEDT(() -> {
            JWindow toast = new JWindow(resolveWindow(parent));
            toast.setBackground(new Color(0, 0, 0, 0));

            JPanel panel = new JPanel(new BorderLayout()) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bgColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(borderColor);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                }
            };
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(12, 18, 12, 18));

            JLabel label = new JLabel(message);
            label.setFont(ThemeConstants.fontBodyBold);
            label.setForeground(Color.WHITE);
            panel.add(label, BorderLayout.CENTER);

            toast.add(panel);
            toast.pack();

            // Position: bottom-right corner of the screen
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int x = screen.width  - toast.getWidth()  - 24;
            int y = screen.height - toast.getHeight() - 60;
            toast.setLocation(x, y);

            // Only set opacity if the platform supports it; skip gracefully if not
            try { toast.setOpacity(1.0f); } catch (UnsupportedOperationException ignored) {}
            toast.setVisible(true);

            // Timer 1: hold, then start fade-out
            Timer holdTimer = new Timer(toastDurationMS, null);
            holdTimer.setRepeats(false);
            holdTimer.addActionListener(e -> {
                holdTimer.stop();

                float[] opacity = {1.0f};
                float   step    = 1.0f / fadeSteps;

                Timer fadeTimer = new Timer(fadeStepsMS, null);
                fadeTimer.addActionListener(evt -> {
                    opacity[0] -= step;
                    if (opacity[0] <= 0f) {
                        toast.setVisible(false);
                        toast.dispose();
                        ((Timer) evt.getSource()).stop();
                    } else {
                        try { toast.setOpacity(Math.max(0f, opacity[0])); }
                        catch (UnsupportedOperationException ignored) {}
                    }
                });
                fadeTimer.start();
            });
            holdTimer.start();
        });
    }


    private static Window resolveWindow(Component c) {
        if (c == null) return null;
        if (c instanceof Window) return (Window) c;
        return SwingUtilities.getWindowAncestor(c);
    }

    private static void runOnEDT(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private static JLabel styledMessagePanel(String message, Color textColor) {
        JLabel label = new JLabel(
                "<html><body style='width:280px; font-family:SansSerif; font-size:13px'>"
                        + message + "</body></html>");
        label.setForeground(textColor);
        label.setBorder(new EmptyBorder(4, 4, 4, 4));
        return label;
    }
}