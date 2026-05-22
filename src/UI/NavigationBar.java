package UI;
import Model.*;
import Helper.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;


public class NavigationBar extends JPanel {
    public static final String sectionDashboard = "dashboard";
    public static final String sectionWorldView = "world view";
    public static final String sectionEntryDetail = "entry detail";
    public static final String sectionEntryEditor = "entry editor";

    private static final String crumbSeparator = " › ";
    private final MainFrame mainFrame;
    private final JPanel breadcrumbPanel;

    private World currentWorld;

    public NavigationBar(MainFrame mainFrame){
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(ThemeConstants.colorSurface);
        setPreferredSize(new Dimension(0, 46));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 9, ThemeConstants.colorBorder),
                new EmptyBorder(0, ThemeConstants.padding*2, 0, ThemeConstants.padding*2)
        ));

        //Left: app name anchor (always navigates home)
        JLabel appLabel = new JLabel("✦ Nexus Codex");
        appLabel.setFont(ThemeConstants.fontBodyBold);
        appLabel.setForeground(ThemeConstants.colorAccent);
        appLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        appLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                mainFrame.navigateToDashboard();
            }
            public void mouseEntered(MouseEvent e){
                appLabel.setForeground(ThemeConstants.colorAccentDark);
            }
            public void mouseExited(MouseEvent e){
                appLabel.setForeground(ThemeConstants.colorAccent);
            }
        });

        breadcrumbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        breadcrumbPanel.setOpaque(false);

        JPanel breadcrumbWrapper = new JPanel(new BorderLayout());
        breadcrumbWrapper.setOpaque(false);
        breadcrumbWrapper.setBorder(new EmptyBorder(0, 24, 0, 0)); // pushes breadcrumbs right

        breadcrumbWrapper.add(breadcrumbPanel, BorderLayout.WEST);

        add(appLabel, BorderLayout.WEST);
        add(breadcrumbWrapper, BorderLayout.CENTER);

        highlight(sectionDashboard, null, null);
    }

    //Public API
    public void highlight(String section, World world, String entryLabel){
        this.currentWorld = world;
        breadcrumbPanel.removeAll();

        switch(section){
            case sectionDashboard:
                addActiveCrumb("My Worlds");
                break;

            case sectionWorldView:
                addClickableCrumb("My Worlds", ()-> mainFrame.navigateToDashboard());
                addSeparator();
                addActiveCrumb(world != null ? world.getName() : "World");
                break;

            case sectionEntryDetail:
                addClickableCrumb("My Worlds", ()-> mainFrame.navigateToDashboard());
                addSeparator();
                addClickableCrumb(world!= null ? world.getName() : "World", ()-> mainFrame.navigateToWorldView());
                addSeparator();
                addActiveCrumb(entryLabel != null ? entryLabel : "Entry");
                break;

            case sectionEntryEditor:
                addClickableCrumb("My Worlds", () -> mainFrame.navigateToDashboard());
                addSeparator();
                addClickableCrumb(
                        world != null ? world.getName() : "World",
                        () -> mainFrame.navigateToWorldView());
                addSeparator();
                addActiveCrumb(entryLabel != null ? entryLabel : "New Entry");
                break;

            default:
                addActiveCrumb("My Worlds");
                break;
        }
        breadcrumbPanel.revalidate();
        breadcrumbPanel.repaint();
    }

    private void addClickableCrumb(String label, Runnable onClick){
        JLabel crumb = new JLabel(label);
        crumb.setFont(ThemeConstants.fontBody);
        crumb.setForeground(ThemeConstants.colorTextSecondary);
        crumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        crumb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                // Underline on hover using HTML
                crumb.setText("<html><u>" + label + "</u></html>");
                crumb.setForeground(ThemeConstants.colorTextPrimary);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                crumb.setText(label);
                crumb.setForeground(ThemeConstants.colorTextSecondary);
            }
        });

        breadcrumbPanel.add(crumb);
    }

    private void addActiveCrumb(String label){
        JLabel crumb = new JLabel(label);
        crumb.setFont(ThemeConstants.fontBodyBold);
        crumb.setForeground(ThemeConstants.colorTextPrimary);
        breadcrumbPanel.add(crumb);
    }

    private void addSeparator(){
        JLabel sep = new JLabel(crumbSeparator);
        sep.setFont(ThemeConstants.fontBody);
        sep.setForeground(ThemeConstants.colorTextPlaceholder);
        breadcrumbPanel.add(sep);
    }
}
