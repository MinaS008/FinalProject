package UI;

import Controller.AnalyticsEngine;
import Controller.AnalyticsEngine.WorldStats;
import Helper.ThemeConstants;
import Model.World;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class AnalyticsPanel extends JPanel {
    private final MainFrame mainFrame;
    private World currentWorld;

    private JPanel bodyPanel;
    private JScrollPane scrollPane;
    private JLabel titleLabel;

    public AnalyticsPanel(MainFrame mainFrame){
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.colorBackground);
        setBorder(new EmptyBorder(ThemeConstants.padding*2, ThemeConstants.padding*2, ThemeConstants.padding*2, ThemeConstants.padding*2));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollBody(), BorderLayout.CENTER);
    }

    private JPanel buildHeader(){
        JButton backButton = buildStyledButton("<- Back", ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        backButton.setForeground(ThemeConstants.colorTextSecondary);
        backButton.addActionListener(e-> mainFrame.navigateToWorldView());

        titleLabel = new JLabel("World Analytics");
        titleLabel.setFont(ThemeConstants.fontTitle);
        titleLabel.setForeground(ThemeConstants.colorAccent);

        JPanel leftBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBlock.setOpaque(false);
        leftBlock.add(backButton);
        leftBlock.add(titleLabel);

        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeConstants.colorBorder);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, ThemeConstants.padding, 0));
        wrapper.add(leftBlock, BorderLayout.WEST);
        wrapper.add(sep, BorderLayout.SOUTH);
        return wrapper;
    }

    private JScrollPane buildScrollBody(){
        bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(ThemeConstants.colorBackground);

        scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    //Public API
    public void loadWorld(World world){
        this.currentWorld = world;
        titleLabel.setText("World Analytics: " + world.getName());
        rebuild();
    }

    public void refresh(){
        if(currentWorld != null) rebuild();
    }

    private void rebuild(){
        bodyPanel.removeAll();

        WorldStats stats = AnalyticsEngine.compute(currentWorld);

        bodyPanel.add(buildOverviewRow(stats));
        bodyPanel.add(spacer(ThemeConstants.padding));
        bodyPanel.add(buildTypeBreakdownSection(stats));
        bodyPanel.add(spacer(ThemeConstants.padding));
        bodyPanel.add(buildDistributionRow(stats));
        bodyPanel.add(spacer(ThemeConstants.padding));
        bodyPanel.add(buildDetailsSection(stats));
        bodyPanel.add(spacer(ThemeConstants.padding));
        bodyPanel.add(buildComplexitySection(stats));
        bodyPanel.add(spacer(ThemeConstants.padding * 2));

        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(0));
        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    //Overview cards
    private JPanel buildOverviewRow(WorldStats stats){
        JPanel row = new JPanel(new GridLayout(1, 4, ThemeConstants.padding, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        row.add(buildStatCard("Total Entries", String.valueOf(stats.totalEntries), null));
        row.add(buildStatCard("Relationships", String.valueOf(stats.relationshipCount), null));
        row.add(buildStatCard("Added Today", String.valueOf(stats.createdTodayCount), null));
        row.add(buildStatCard("Last Updated", stats.mostRecentEntryName, null));
        return row;
    }

    private JPanel buildStatCard(String label, String value, Color accentColor){
        JPanel card = new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeConstants.colorSurface);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                g2.setColor(ThemeConstants.colorBorder);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                g2.dispose();
            }
        };

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(ThemeConstants.padding, ThemeConstants.padding, ThemeConstants.padding, ThemeConstants.padding));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Serif", Font.BOLD, 22));
        valueLabel.setForeground(accentColor != null ? accentColor : ThemeConstants.colorAccent);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(ThemeConstants.fontSmall);
        labelComp.setForeground(ThemeConstants.colorTextSecondary);
        labelComp.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(labelComp);
        return card;
    }

    private JPanel buildTypeBreakdownSection(WorldStats stats){
        JPanel section = buildSection("Entry Breakdown by Type");
        int total = stats.totalEntries;

        for(String type: WorldStats.typeOrder){
            int count = stats.countForType(type);
            double pct = total > 0 ? (double) count / total : 0.0;
            section.add(buildTypeBar(type, count, pct, typeColor(type)));
            section.add(spacer(8));
        }
        return section;
    }

    private JPanel buildTypeBar(String type, int count, double fraction, Color color){
        JPanel row = new JPanel(new BorderLayout(ThemeConstants.padding, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel nameLabel = new JLabel(type);
        nameLabel.setFont(ThemeConstants.fontSmall);
        nameLabel.setForeground(ThemeConstants.colorTextPrimary);
        nameLabel.setPreferredSize(new Dimension(90, 20));

        JProgressBar bar = new JProgressBar(0, 100){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Track
                g2.setColor(ThemeConstants.colorSurfaceHover);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                // Fill
                int fillW = (int) (getWidth() * fraction);
                if (fillW > 0) {
                    g2.setColor(color);
                    g2.fillRoundRect(0, 0, fillW, getHeight(), 6, 6);
                }
                g2.dispose();
            }
        };

        bar.setValue((int) (fraction*100));
        bar.setOpaque(false);
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(80, 20));

        JLabel countLabel = new JLabel(count + " (" + Math.round(fraction*100) + "%)");
        countLabel.setFont(ThemeConstants.fontSmall);
        countLabel.setForeground(ThemeConstants.colorTextSecondary);
        countLabel.setPreferredSize(new Dimension(80, 20));
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(nameLabel, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(countLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel buildDistributionRow(WorldStats stats) {
        JPanel row = new JPanel(new GridLayout(1, 2, ThemeConstants.padding, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        row.add(buildMapSection("Item Rarity Distribution", stats.itemRarityDistribution,
                stats.countItems));
        row.add(buildMapSection("Lore Era Distribution", stats.loreEraDistribution,
                stats.countLore));

        return row;
    }

    private JPanel buildMapSection(String title, Map<String, Integer> distribution, int total) {
        JPanel section = buildSection(title);

        if (distribution.isEmpty()) {
            JLabel none = new JLabel("No data yet.");
            none.setFont(ThemeConstants.fontBody);
            none.setForeground(ThemeConstants.colorTextSecondary);
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            section.add(none);
            return section;
        }

        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            double pct = total > 0 ? (double) entry.getValue() / total : 0.0;
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

            JLabel key = new JLabel(entry.getKey());
            key.setFont(ThemeConstants.fontSmall);
            key.setForeground(ThemeConstants.colorTextPrimary);
            key.setPreferredSize(new Dimension(100, 18));

            JLabel val = new JLabel(entry.getValue() + "  (" + Math.round(pct * 100) + "%)");
            val.setFont(ThemeConstants.fontSmall);
            val.setForeground(ThemeConstants.colorTextSecondary);
            val.setHorizontalAlignment(SwingConstants.RIGHT);
            val.setPreferredSize(new Dimension(80, 18));

            row.add(key, BorderLayout.WEST);
            row.add(val, BorderLayout.EAST);
            section.add(row);
            section.add(spacer(4));
        }
        return section;
    }

    private JPanel buildDetailsSection(WorldStats stats){
        JPanel section = buildSection("World Details");

        section.add(buildDetailRow("Avg. abilities per character",
                String.format("%.1f", stats.avgAbilitiesPerCharacter)));
        section.add(spacer(6));
        section.add(buildDetailRow("Avg. members per faction",
                String.format("%.1f", stats.avgMembersPerFaction)));
        section.add(spacer(6));
        section.add(buildDetailRow("Relationship links",
                String.valueOf(stats.relationshipCount)));
        return section;
    }

    private JPanel buildDetailRow(String label, String value){
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeConstants.fontSmall);
        lbl.setForeground(ThemeConstants.colorTextSecondary);

        JLabel val = new JLabel(value);
        val.setFont(ThemeConstants.fontBodyBold);
        val.setForeground(ThemeConstants.colorTextPrimary);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);

        return row;
    }

    private JPanel buildComplexitySection(WorldStats stats){
        JPanel section = buildSection("World Complexity Score");

        int score = stats.complexityScore;
        String tier;
        Color tierColor;
        if (score >= 300) { tier = "Legendary"; tierColor = new Color(196, 160, 80); }
        else if (score >= 150) { tier = "Epic"; tierColor = new Color(130, 70, 160); }
        else if (score >= 75)  { tier = "Established"; tierColor = new Color(70, 150, 100); }
        else if (score >= 25)  { tier = "Developing"; tierColor = new Color(80, 120, 180); }
        else { tier = "Nascent"; tierColor = ThemeConstants.colorTextSecondary; }

        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        scoreRow.setOpaque(false);
        scoreRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel scoreLabel = new JLabel(String.valueOf(score));
        scoreLabel.setFont(new Font("Serif", Font.BOLD, 36));
        scoreLabel.setForeground(tierColor);

        JPanel tierBlock = new JPanel();
        tierBlock.setLayout(new BoxLayout(tierBlock, BoxLayout.Y_AXIS));
        tierBlock.setOpaque(false);

        JLabel tierLabel = new JLabel(tier);
        tierLabel.setFont(ThemeConstants.fontHeading);
        tierLabel.setForeground(tierColor);

        JLabel hint = new JLabel("Add more entries, relationships, and details to grow your score.");
        hint.setFont(ThemeConstants.fontSmall);
        hint.setForeground(ThemeConstants.colorTextSecondary);

        tierBlock.add(tierLabel);
        tierBlock.add(hint);

        scoreRow.add(scoreLabel);
        scoreRow.add(tierBlock);
        section.add(scoreRow);

        return section;
    }

    private JPanel buildSection(String title){
        JPanel section = new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeConstants.colorSurface);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                g2.setColor(ThemeConstants.colorBorder);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                g2.dispose();
            }
        };
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        section.setBorder(new EmptyBorder(ThemeConstants.padding, ThemeConstants.padding,
                ThemeConstants.padding, ThemeConstants.padding));

        JLabel heading = new JLabel(title);
        heading.setFont(ThemeConstants.fontHeading);
        heading.setForeground(ThemeConstants.colorAccent);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(heading);
        section.add(spacer(10));
        return section;
    }

    private Component spacer(int h){
        JPanel sp = new JPanel();
        sp.setOpaque(false);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        sp.setPreferredSize(new Dimension(0, h));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sp;
    }

    private Color typeColor(String type){
        switch (type) {
            case "Character": return new Color(80, 120, 180);
            case "Location": return new Color(70, 150, 100);
            case "Item": return new Color(160, 100, 50);
            case "Faction": return new Color(130, 70, 160);
            case "Lore": return new Color(160, 80, 80);
            default: return ThemeConstants.colorSurfaceHover;
        }
    }

    private JButton buildStyledButton(String text, Color bg, Color hover){
        JButton button = new JButton(text) {
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
        button.setFont(ThemeConstants.fontButton);
        button.setForeground(ThemeConstants.colorTextSecondary);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(6, 14, 6, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(hover);  button.repaint(); }
            public void mouseExited(MouseEvent e)  { button.setBackground(bg);     button.repaint(); }
        });
        return button;
    }
}
