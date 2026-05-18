package UI;
import Persistence.*;
import Model.*;
import Helper.*;
import Utilities.*;
import Controller.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class EntryDetailPanel extends JPanel {
    private final MainFrame mainFrame;
    private final WorldManager worldManager;

    private World currentWorld;
    private CodexEntry currentEntry;

    private JLabel entryNameLabel;
    private JLabel typeBadgeLabel;
    private JPanel detailBody;
    private JScrollPane scrollPane;

    public EntryDetailPanel(MainFrame mainFrame, WorldManager worldManager) {
        this.mainFrame = mainFrame;
        this.worldManager = worldManager;

        setLayout(new BorderLayout());
        setBackground(ThemeConstants.colorBackground);
        setBorder(new EmptyBorder(
                ThemeConstants.padding * 2, ThemeConstants.padding * 2,
                ThemeConstants.padding * 2, ThemeConstants.padding * 2));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollBody(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        typeBadgeLabel = new JLabel("") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        typeBadgeLabel.setFont(ThemeConstants.fontSmall);
        typeBadgeLabel.setForeground(Color.WHITE);
        typeBadgeLabel.setOpaque(false);
        typeBadgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        typeBadgeLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        typeBadgeLabel.setPreferredSize(new Dimension(90, 26));

        //Entry name
        entryNameLabel = new JLabel("-");
        entryNameLabel.setFont(ThemeConstants.fontTitle);
        entryNameLabel.setForeground(ThemeConstants.colorTextPrimary);

        //Back button
        JButton backButton = buildStyledButton("← Back",
                ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        backButton.setForeground(ThemeConstants.colorTextSecondary);
        backButton.addActionListener(e -> mainFrame.navigateToWorldView());

        //Left block
        JPanel leftBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBlock.setOpaque(false);
        leftBlock.add(backButton);
        leftBlock.add(typeBadgeLabel);
        leftBlock.add(entryNameLabel);

        //Right block
        JButton editButton = buildStyledButton("✎ Edit",
                ThemeConstants.colorAccent, ThemeConstants.colorAccentDark);
        editButton.addActionListener(e -> handleEdit());

        JButton deleteButton = buildStyledButton("✕ Delete",
                ThemeConstants.colorDanger, ThemeConstants.colorDangerDark);
        deleteButton.addActionListener(e -> handleDelete());

        JPanel rightBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBlock.setOpaque(false);
        rightBlock.add(editButton);
        rightBlock.add(deleteButton);

        //Assemble header row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(leftBlock, BorderLayout.WEST);
        headerRow.add(rightBlock, BorderLayout.EAST);

        // Separator below
        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeConstants.colorBorder);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, ThemeConstants.padding, 0));
        wrapper.add(headerRow, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);

        return wrapper;
    }

    private JScrollPane buildScrollBody() {
        detailBody = new JPanel();
        detailBody.setLayout(new BoxLayout(detailBody, BoxLayout.Y_AXIS));
        detailBody.setBackground(ThemeConstants.colorBackground);
        detailBody.setBorder(new EmptyBorder(ThemeConstants.padding, 0, 0, 0));

        scrollPane = new JScrollPane(detailBody);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    //Public API
    public void loadEntry(World world, CodexEntry entry) {
        this.currentWorld = world;
        this.currentEntry = entry;
        refresh();
    }

    public void refresh() {
        if (currentEntry == null) return;

        //Update header labels
        entryNameLabel.setText(currentEntry.getName());
        typeBadgeLabel.setText(currentEntry.getType());
        typeBadgeLabel.setBackground(badgeColorFor(currentEntry.getType()));

        //Rebuild detail body
        detailBody.removeAll();
        detailBody.add(buildDescriptionSection());
        detailBody.add(spacer(12));
        detailBody.add(buildTypeSpecificSection());
        detailBody.add(spacer(12));
        detailBody.add(buildTimestampSection());
        detailBody.add(Box.createVerticalGlue());

        //Scroll to top
        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(0));

        detailBody.revalidate();
        detailBody.repaint();
    }

    private JPanel buildDescriptionSection() {
        JPanel section = buildSection("Description");
        String desc = currentEntry.getDescription();
        section.add(buildBodyText(desc.isBlank() ? "No description provided." : desc));
        return section;
    }

    private JPanel buildTypeSpecificSection() {
        switch (currentEntry.getType()) {
            case "Character":
                return buildCharacterSection((Model.Character) currentEntry);
            case "Location":
                return buildLocationSection((Location) currentEntry);
            case "Item":
                return buildItemSection((Item) currentEntry);
            case "Faction":
                return buildFactionSection((Faction) currentEntry);
            case "Lore":
                return buildLoreSection((LoreEntry) currentEntry);
            default:
                return new JPanel();
        }
    }

    //Sections
    private JPanel buildCharacterSection(Model.Character c) {
        JPanel section = buildSection("Character Details");
        section.add(buildFieldRow("Role", c.getRole()));
        section.add(spacer(6));
        section.add(buildFieldRow("Backstory", c.getBackstory().isBlank()
                ? "None recorded." : c.getBackstory()));
        section.add(spacer(6));
        section.add(buildListField("Affiliations", c.getAffiliations()));
        section.add(spacer(6));
        section.add(buildListField("Abilities", c.getAbilities()));
        section.add(spacer(6));
        section.add(buildListField("Relationships", c.getRelationships()));
        return section;
    }

    private JPanel buildLocationSection(Model.Location loc) {
        JPanel section = buildSection("Location Details");
        section.add(buildFieldRow("Type", loc.getLocationType()));
        section.add(spacer(6));
        section.add(buildFieldRow("Region", loc.getRegion().isBlank()
                ? "None recorded." : loc.getRegion()));
        section.add(spacer(6));
        section.add(buildListField("Sub-locations", loc.getSubLocations()));
        section.add(spacer(6));
        section.add(buildListField("Connections", loc.getConnections()));
        return section;
    }

    private JPanel buildItemSection(Model.Item item) {
        JPanel section = buildSection("Item Details");
        section.add(buildFieldRow("Type", item.getItemType()));
        section.add(spacer(6));
        section.add(buildFieldRow("Rarity", item.getRarity()));
        section.add(spacer(6));
        section.add(buildFieldRow("Power", item.getPower().isBlank()
                ? "None described." : item.getPower()));
        section.add(spacer(6));
        section.add(buildFieldRow("Current Owner", item.getCurrentOwner()));
        section.add(spacer(6));
        section.add(buildListField("Ownership History", item.getOwnerHistory()));
        return section;
    }

    private JPanel buildFactionSection(Model.Faction f) {
        JPanel section = buildSection("Faction Details");
        section.add(buildFieldRow("Goal", f.getGoal().isBlank()
                ? "None recorded." : f.getGoal()));
        section.add(spacer(6));
        section.add(buildFieldRow("Ideology", f.getIdeology().isBlank()
                ? "None recorded." : f.getIdeology()));
        section.add(spacer(6));
        section.add(buildListField("Members", f.getMembers()));
        section.add(spacer(6));
        section.add(buildListField("Faction Relationships", f.getFactionRelationships()));
        return section;
    }

    private JPanel buildLoreSection(Model.LoreEntry lore) {
        JPanel section = buildSection("Lore Details");
        section.add(buildFieldRow("Era", lore.getEra()));
        section.add(spacer(6));
        section.add(buildListField("Timeline", lore.getTimeline()));
        section.add(spacer(6));
        section.add(buildListField("Consequences", lore.getConsequences()));
        section.add(spacer(6));
        section.add(buildListField("References", lore.getReferences()));
        return section;
    }

    private JPanel buildTimestampSection() {
        JPanel section = buildSection("Record Info");
        section.add(buildFieldRow("Created", currentEntry.getCreatedAt()));
        section.add(spacer(4));
        section.add(buildFieldRow("Last Updated", currentEntry.getUpdatedAt()));
        return section;
    }

    private void handleEdit() {
        mainFrame.navigateToEditor(currentWorld, currentEntry);
    }

    private void handleDelete(){
        int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                "Delete \"" + currentEntry.getName() + "\"?\nThis cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            worldManager.deleteEntry(currentWorld.getID(), currentEntry.getID());
            mainFrame.saveWorld(currentWorld);
            mainFrame.navigateToWorldView();
        }
    }

    private JPanel buildSection(String title){
        JPanel section = new JPanel(){
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

        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        section.setBorder(new EmptyBorder(
                ThemeConstants.padding, ThemeConstants.padding,
                ThemeConstants.padding, ThemeConstants.padding));

        JLabel heading = new JLabel(title);
        heading.setFont(ThemeConstants.fontHeading);
        heading.setForeground(ThemeConstants.colorAccent);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(heading);
        section.add(spacer(8));

        return section;
    }

    private JPanel buildFieldRow(String label, String value){
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelComp = new JLabel(label.toUpperCase());
        labelComp.setFont(ThemeConstants.fontSmall);
        labelComp.setForeground(ThemeConstants.colorTextPrimary);
        labelComp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueComp = new JLabel(
                "<html><body style='width:600px'>" + value + "</body></html>");
        valueComp.setFont(ThemeConstants.fontBody);
        valueComp.setForeground(ThemeConstants.colorTextPrimary);
        valueComp.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(labelComp);
        row.add(Box.createVerticalStrut(2));
        row.add(valueComp);
        return row;
    }

    private JPanel buildListField(String label, List <String> items){
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelComp = new JLabel(label.toUpperCase());
        labelComp.setFont(ThemeConstants.fontSmall);
        labelComp.setForeground(ThemeConstants.colorTextPlaceholder);
        labelComp.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(labelComp);
        row.add(Box.createVerticalStrut(2));

        if (items.isEmpty()) {
            JLabel none = new JLabel("None.");
            none.setFont(ThemeConstants.fontBody);
            none.setForeground(ThemeConstants.colorTextSecondary);
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.add(none);
        } else {
            for (String item : items) {
                JLabel bullet = new JLabel("• " + item);
                bullet.setFont(ThemeConstants.fontBody);
                bullet.setForeground(ThemeConstants.colorTextPrimary);
                bullet.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.add(bullet);
            }
        }

        return row;
    }

    private JLabel buildBodyText(String text){
        JLabel label = new JLabel(
                "<html><body style='width:600px'>" + text + "</body></html>");
        label.setFont(ThemeConstants.fontBody);
        label.setForeground(ThemeConstants.colorTextPrimary);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private Component spacer(int height){
        JPanel sp = new JPanel();
        sp.setOpaque(false);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        sp.setPreferredSize(new Dimension(0, height));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sp;
    }

    private Color badgeColorFor(String type){
        switch(type){
            case "Character": return new Color(80, 120, 180);
            case "Location": return new Color(70, 150, 100);
            case "Item": return new Color(160, 100, 50);
            case "Faction": return new Color(130, 70, 160);
            case "Lore": return new Color(160, 80, 80);
            default: return ThemeConstants.colorSurfaceHover;
        }
    }

    private JButton buildStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
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
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(6, 14, 6, 14));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
                button.repaint();
            }
        });
        return button;
    }
}