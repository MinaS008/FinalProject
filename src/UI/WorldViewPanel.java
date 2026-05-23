package UI;
import Utilities.*;
import Persistence.*;
import Model.*;
import Helper.*;
import Controller.*;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class WorldViewPanel extends JPanel {
    private static final String tabAll = "All";
    private static final String tabCharacter = "Characters";
    private static final String tabFaction = "Factions";
    private static final String tabItem = "Items";
    private static final String tabLore = "Lore";
    private static final String tabLocation = "Locations";

    private static final String[] tabNames = {
            tabAll, tabCharacter, tabFaction, tabItem, tabLore, tabLocation
    };

    private final MainFrame mainFrame;
    private final WorldManager worldManager;

    private World currentWorld;
    private String activeTab = tabAll;

    private String activeQuery = "";
    private String activeSortMode = SearchEngine.sortNameAsc;

    private JLabel worldNameLabel;
    private JLabel entryCountLabel;
    private JPanel tabBar;
    private JPanel entryListPanel;
    private JScrollPane scrollPane;
    private JTextField searchField;
    private JLabel resultCountLabel;

    public WorldViewPanel(MainFrame mainFrame, WorldManager worldManager) {
        this.mainFrame = mainFrame;
        this.worldManager = worldManager;

        setLayout(new BorderLayout());
        setBackground(ThemeConstants.colorBackground);
        setBorder(new EmptyBorder(
                ThemeConstants.padding * 2, ThemeConstants.padding * 2,
                ThemeConstants.padding * 2, ThemeConstants.padding * 2));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenterSection(), BorderLayout.CENTER);
    }

    public JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(ThemeConstants.padding, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, ThemeConstants.padding, 0));

        JButton backButton = buildStyleButton(" <- Back", ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        backButton.setForeground(ThemeConstants.colorTextSecondary);
        backButton.addActionListener(e -> mainFrame.navigateToDashboard());

        //World Item
        worldNameLabel = new JLabel("-");
        worldNameLabel.setFont(ThemeConstants.fontTitle);
        worldNameLabel.setForeground(ThemeConstants.colorAccent);

        entryCountLabel = new JLabel("");
        entryCountLabel.setFont(ThemeConstants.fontSmall);
        entryCountLabel.setForeground(ThemeConstants.colorTextSecondary);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(worldNameLabel);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(entryCountLabel);

        JSeparator separator = new JSeparator();
        separator.setForeground(ThemeConstants.colorBorder);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JButton newEntryButton = buildStyleButton("+ New Entry", ThemeConstants.colorAccent, ThemeConstants.colorAccentDark);
        newEntryButton.setForeground(Color.WHITE);
        newEntryButton.addActionListener(e -> {
            if (currentWorld != null) mainFrame.navigateToCreateEntry(currentWorld);
        });

        JButton relButton = buildStyleButton("⬡ Relationships", ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        relButton.setForeground(ThemeConstants.colorTextSecondary);
        relButton.addActionListener(e -> {
            if (currentWorld != null) mainFrame.navigateToRelationships(currentWorld);
        });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(relButton);
        buttonRow.add(newEntryButton);

        JPanel topRow = new JPanel(new BorderLayout(ThemeConstants.padding, 0));
        topRow.setOpaque(false);
        topRow.setBorder(new EmptyBorder(0, 0, ThemeConstants.padding, 0));
        topRow.add(backButton, BorderLayout.WEST);
        topRow.add(titleBlock, BorderLayout.CENTER);
        topRow.add(buttonRow, BorderLayout.EAST);

        wrapper.add(topRow, BorderLayout.CENTER);
        wrapper.add(separator, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel buildCenterSection() {
        JPanel center = new JPanel(new BorderLayout(0, ThemeConstants.padding));
        center.setOpaque(false);

        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);

        tabBar = buildTabBar();
        northStack.add(tabBar);
        northStack.add(Box.createVerticalStrut(ThemeConstants.padding));
        northStack.add(buildSearchBar());

        center.add(northStack, BorderLayout.NORTH);

        entryListPanel = new JPanel();
        entryListPanel.setLayout(new BoxLayout(entryListPanel, BoxLayout.Y_AXIS));
        entryListPanel.setBackground(ThemeConstants.colorBackground);

        scrollPane = new JScrollPane(entryListPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        center.add(scrollPane, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout(ThemeConstants.padding, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // Search field with placeholder
        searchField = new JTextField();
        searchField.setFont(ThemeConstants.fontBody);
        searchField.setForeground(ThemeConstants.colorTextPlaceholder);
        searchField.setBackground(ThemeConstants.colorSurface);
        searchField.setCaretColor(ThemeConstants.colorTextPrimary);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConstants.colorBorder),
                new javax.swing.border.EmptyBorder(6, 10, 6, 10)));
        searchField.setText("Search entries...");

        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search entries...")) {
                    searchField.setText("");
                    searchField.setForeground(ThemeConstants.colorTextPrimary);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchField.setText("Search entries...");
                    searchField.setForeground(ThemeConstants.colorTextPlaceholder);
                }
            }
        });

        // Live search: fire on every keystroke via DocumentListener
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { onSearchChanged(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { onSearchChanged(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onSearchChanged(); }
        });

        // Sort dropdown
        JComboBox<String> sortCombo = new JComboBox<>(SearchEngine.sortOptions);
        sortCombo.setFont(ThemeConstants.fontSmall);
        sortCombo.setBackground(ThemeConstants.colorSurface);
        sortCombo.setForeground(ThemeConstants.colorTextPrimary);
        sortCombo.setSelectedItem(activeSortMode);
        sortCombo.setPreferredSize(new Dimension(160, 34));
        sortCombo.addActionListener(e -> {
            activeSortMode = (String) sortCombo.getSelectedItem();
            populateEntryList();
        });

        // Result count label (right of sort combo)
        resultCountLabel = new JLabel("");
        resultCountLabel.setFont(ThemeConstants.fontSmall);
        resultCountLabel.setForeground(ThemeConstants.colorTextSecondary);

        JPanel rightBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBlock.setOpaque(false);
        rightBlock.add(resultCountLabel);
        rightBlock.add(sortCombo);

        bar.add(searchField, BorderLayout.CENTER);
        bar.add(rightBlock, BorderLayout.EAST);

        return bar;
    }

    private void onSearchChanged(){
        String raw = searchField.getText();
        if(raw.equals("Search entries...") || raw.isBlank()){
            activeQuery = "";
        } else {
            Validator.ValidationResult result = Validator.validateSearchQuery(raw);
            if(!result.isValid()){
                resultCountLabel.setText("Query too long");
                return;
            }
            activeQuery = raw;
        }
        populateEntryList();
    }

    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(ThemeConstants.padding, 0, 0, 0));

        for (String tabName : tabNames) {
            JButton tab = buildTabButton(tabName);
            bar.add(tab);
        }

        return bar;
    }

    private JButton buildTabButton(String tabName) {
        boolean isActive = tabName.equals(activeTab);

        JButton tab = new JButton(tabName) {
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
        tab.setFont(ThemeConstants.fontButton);
        tab.setFocusPainted(false);
        tab.setBorderPainted(false);
        tab.setContentAreaFilled(false);
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(6, 14, 6, 14));
        tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (isActive) {
            tab.setBackground(ThemeConstants.colorAccent);
            tab.setForeground(Color.WHITE);
        } else {
            tab.setBackground(ThemeConstants.colorSurface);
            tab.setForeground(ThemeConstants.colorTextPrimary);

            tab.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    tab.setBackground(ThemeConstants.colorSurfaceHover);
                    tab.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    tab.setBackground(ThemeConstants.colorSurface);
                    tab.repaint();
                }
            });
        }

        tab.addActionListener(e -> selectTab(tabName));
        return tab;
    }

    private void selectTab(String tabName) {
        activeTab = tabName;
        refreshTabBar();
        populateEntryList();
    }

    private void refreshTabBar() {
        tabBar.removeAll();
        for (String tabName : tabNames) {
            tabBar.add(buildTabButton(tabName));
        }
        tabBar.revalidate();
        tabBar.repaint();
    }

    private void populateEntryList() {
        entryListPanel.removeAll();

        if (currentWorld == null) {
            entryListPanel.revalidate();
            entryListPanel.repaint();
            return;
        }

        List<CodexEntry> allEntries = worldManager.getEntries(currentWorld.getID(), null);
        String typeFilter = activeTab.equals(tabAll) ? null : tabTypeFor(activeTab);

        List<CodexEntry> results = SearchEngine.search(allEntries, activeQuery, typeFilter, activeSortMode);


        if (resultCountLabel != null) {
            if (activeQuery.isEmpty()) {
                resultCountLabel.setText("");
            } else {
                resultCountLabel.setText(results.size()
                        + " result" + (results.size() == 1 ? "" : "s"));
            }
        }

        if (results.isEmpty()) {
            entryListPanel.add(buildEmptyState());
        } else {
            for (CodexEntry entry : results) {
                entryListPanel.add(buildEntryRow(entry));
                entryListPanel.add(Box.createVerticalStrut(8));
            }
        }

        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(0));

        entryListPanel.revalidate();
        entryListPanel.repaint();
    }

    private String tabTypeFor(String tabName) {
        switch (tabName) {
            case tabCharacter:
                return "Character";
            case tabLocation:
                return "Location";
            case tabItem:
                return "Item";
            case tabFaction:
                return "Faction";
            case tabLore:
                return "Lore";
            default:
                return null;
        }
    }

    private JPanel buildEntryRow(CodexEntry entry) {
        JPanel row = new JPanel(new BorderLayout(ThemeConstants.padding, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                g2.setColor(ThemeConstants.colorBorder);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBackground(ThemeConstants.colorSurface);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setPreferredSize(new Dimension(0, 64));
        row.setBorder(new EmptyBorder(ThemeConstants.padding, ThemeConstants.padding,
                ThemeConstants.padding, ThemeConstants.padding));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel typeBadge = buildTypeBadge(entry.getType());
        JLabel nameLabel = new JLabel(entry.getName());
        nameLabel.setFont(ThemeConstants.fontBodyBold);
        nameLabel.setForeground(ThemeConstants.colorTextPrimary);

        JLabel summaryLabel = new JLabel(entry.getSummary());
        summaryLabel.setFont(ThemeConstants.fontSmall);
        summaryLabel.setForeground(ThemeConstants.colorTextSecondary);

        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);
        textBlock.add(nameLabel);
        textBlock.add(Box.createVerticalStrut(3));
        textBlock.add(summaryLabel);

        // Right: last updated timestamp
        JLabel updatedLabel = new JLabel(entry.getUpdatedAt().substring(0, 10));
        updatedLabel.setFont(ThemeConstants.fontSmall);
        updatedLabel.setForeground(ThemeConstants.colorTextPlaceholder);

        row.add(typeBadge, BorderLayout.WEST);
        row.add(textBlock, BorderLayout.CENTER);
        row.add(updatedLabel, BorderLayout.EAST);

        // Hover effect
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                row.setBackground(ThemeConstants.colorSurfaceHover);
                row.repaint();
            }
            public void mouseExited(MouseEvent e) {
                row.setBackground(ThemeConstants.colorSurface);
                row.repaint();
            }
            public void mouseClicked(MouseEvent e) {
                mainFrame.navigateToEntry(currentWorld, entry);
            }
        });

        return row;
    }

    private JLabel buildTypeBadge(String type){
        JLabel badge = new JLabel(type){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(ThemeConstants.fontSmall);
        badge.setForeground(Color.WHITE);
        badge.setOpaque(false);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        badge.setPreferredSize(new Dimension(82, 26));
        badge.setBackground(badgeColorFor(type));
        return badge;
    }

    private Color badgeColorFor(String type){
        switch(type){
            case "Character": return new Color(80, 120, 180);   // blue
            case "Location": return new Color(70, 150, 100);   // green
            case "Item": return new Color(160, 100, 50);   // amber
            case "Faction": return new Color(130, 70, 160);   // purple
            case "Lore": return new Color(160, 80, 80);    // red
            default: return ThemeConstants.colorSurfaceHover;
        }
    }

    private JPanel buildEmptyState(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        String message = activeTab.equals(tabAll)
                ? "<html><center>This world has no entries yet.<br>"
                + "Use the entry types above to start building your world.</center></html>"
                : "<html><center>No " + activeTab.toLowerCase() + " yet.</center></html>";

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(ThemeConstants.fontBody);
        label.setForeground(ThemeConstants.colorTextSecondary);
        label.setBorder(new EmptyBorder(60, 0, 0, 0));

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    //Public API
    public void loadWorld(World world){
        this.currentWorld = world;
        this.activeTab = tabAll;
        this.activeQuery = "";
        this.activeSortMode = SearchEngine.sortNameAsc;

        if(searchField != null){
            searchField.setText("Search entries...");
            searchField.setForeground(ThemeConstants.colorTextPlaceholder);
        }

        worldNameLabel.setText(world.getName());
        updateEntryCount();
        refreshTabBar();
        populateEntryList();
    }

    public void refresh(){
        if (currentWorld == null) return;
        updateEntryCount();
        refreshTabBar();
        populateEntryList();
    }

    //Helpers
    private void updateEntryCount(){
        int count = currentWorld.getEntryCount();
        entryCountLabel.setText(count + " entr" + (count == 1 ? "y" : "ies"));
    }

    private JButton buildStyleButton(String text, Color bgColor, Color hoverColor){
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
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(6, 14, 6, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(bgColor);

        button.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){
                button.setBackground(hoverColor);
                button.repaint();
            }
            public void mouseExited(MouseEvent e){
                button.setBackground(bgColor);
                button.repaint();
            }
        });
        return button;
    }
}
