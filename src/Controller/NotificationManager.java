package Controller;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import Helper.*;

public class NotificationManager {
    private static final int toastDurationMS = 2500;
    private static final int fadeStepsMS = 50;
    private static final int fadeSteps = 20;

    private NotificationManager() {}

    public static void showSuccess(Component parent, String message){
        showToast(parent, message,
                new Color(45, 130, 75),
                new Color(35, 100, 55));
    }

    public static void showErrorToast(Component parent, String message){
        showToast(parent, message,
                new Color(160, 50, 50),
                new Color(120, 35, 35));
    }

    private static void showToast(Component parent, String message, Color bgColor, Color border){

        JWindow toast = new JWindow();
        toast.setBackground(new Color( 0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout()){
            protected void paintComponent (Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
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

        //Position: bottom-right corner of the screen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screen.width - toast.getWidth() - 24;
        int y = screen.height - toast.getHeight() - 60;
        toast.setLocation(x, y);
        toast.setOpacity(1.0f);
        toast.setVisible(true);

        //Timer 1: wait, then start fade-out
        Timer holdTimer = new Timer(toastDurationMS, null);
        holdTimer.setRepeats(false);
        holdTimer.addActionListener(e-> {
            holdTimer.stop();

            //Timer 2: fade out in fadeSteps steps
            float[] opacity = {1.0f};
            float step = 1.0f/fadeSteps;

            Timer fadeTimer = new Timer(fadeStepsMS, null);
            fadeTimer.addActionListener(evt -> {
                opacity[0] -= step;
                if(opacity[0] <= 0f){
                    toast.setVisible(false);
                    toast.dispose();
                    ((Timer) evt.getSource()).stop();
                } else {
                    toast.setOpacity(Math.max(0f, opacity[0]));
                }
            });
            fadeTimer.start();
        });
        holdTimer.start();
    }

    public static void showError(Component parent, String message){
        JOptionPane.showMessageDialog(
                parent,
                styledMessagePanel(message, ThemeConstants.colorDanger),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void showWarning(Component parent, String  message){
        JOptionPane.showMessageDialog(
                parent,
                styledMessagePanel(message, new Color(180, 130, 40)),
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    public static boolean showConfirm(Component parent, String message, String title){
        int result = JOptionPane.showConfirmDialog(
                parent,
                styledMessagePanel(message, ThemeConstants.colorTextPrimary),
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    private static JLabel styledMessagePanel(String message, Color textColor){
        JLabel label = new JLabel(
                "<html><body style='width:280px; font-family:SansSerif; font-size:13px'>"
                        + message + "</body></html>");
        label.setForeground(textColor);
        label.setBorder(new EmptyBorder(4, 4, 4, 4));
        return label;

    }
}
