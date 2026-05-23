package UI;
import Controller.WorldManager;
import Helper.*;
import Model.*;
import Utilities.*;
import Controller.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;


public class DashboardPanel extends JPanel {
    private final MainFrame mainFrame;
    private final WorldManager worldManager;

    private JPanel worldGridPanel;

    public DashboardPanel(MainFrame mainFrame, WorldManager worldManager){
        this.mainFrame = mainFrame;
        this.worldManager = worldManager;

        setLayout(new BorderLayout());
        setBackground(ThemeConstants.colorBackground);
        setBorder(new EmptyBorder(ThemeConstants.padding * 2,
                ThemeConstants.padding * 2,
                ThemeConstants.padding * 2,
                ThemeConstants.padding * 2));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollableGrid(), BorderLayout.CENTER);
    }

    private JPanel buildHeader(){
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, ThemeConstants.padding*2, 0));

        //Left: app title
        JLabel titleLabel = new JLabel("The Nexus Codex");
        titleLabel.setFont(ThemeConstants.fontTitle);
        titleLabel.setForeground(ThemeConstants.colorAccent);

        //Right: subtitle + new world button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JLabel subtitleLabel = new JLabel("Your Worlds");
        subtitleLabel.setFont(ThemeConstants.fontHeading);
        subtitleLabel.setForeground(ThemeConstants.colorTextPrimary);

        JButton newWorldButton = buildStyledButton("+ New World",
                ThemeConstants.colorAccent, ThemeConstants.colorAccentDark);
        newWorldButton.addActionListener(e -> showCreateWorldDialog());

        rightPanel.add(subtitleLabel);
        rightPanel.add(newWorldButton);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        //Separator line
        JSeparator separator = new JSeparator();
        separator.setForeground(ThemeConstants.colorBorder);
        separator.setBackground(ThemeConstants.colorBorder);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(header, BorderLayout.CENTER);
        wrapper.add(separator, BorderLayout.SOUTH);

        return wrapper;
    }

    private JScrollPane buildScrollableGrid(){
        worldGridPanel = new JPanel();
        worldGridPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 16, 16));
        worldGridPanel.setBackground(ThemeConstants.colorBackground);

        JScrollPane scrollPane = new JScrollPane(worldGridPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        populateGrid();
        return scrollPane;
    }

    private void populateGrid(){
        worldGridPanel.removeAll();

        List<World> worlds = worldManager.getAllWorlds();

        if(worlds.isEmpty()){
            worldGridPanel.add(buildEmptyStateLabel());
        } else {
            for(World world : worlds){
                worldGridPanel.add(buildWorldCard(world));
            }
        }

        worldGridPanel.revalidate();
        worldGridPanel.repaint();
    }

    private JPanel buildWorldCard(World world){
        JPanel card = new JPanel(new BorderLayout(0, 8)){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeConstants.colorSurface);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                g2.setColor(ThemeConstants.colorBorder);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(280, 160));
        card.setBorder(new EmptyBorder(ThemeConstants.padding, ThemeConstants.padding, ThemeConstants.padding, ThemeConstants.padding));

        JLabel nameLabel = new JLabel(world.getName());
        nameLabel.setFont(ThemeConstants.fontBodyBold);
        nameLabel.setForeground(ThemeConstants.colorAccent);

        String desc = world.getDescription();
        String shortDesc = desc.length() > 60 ? desc.substring(0, 60) + "..." : desc;

        JLabel descLabel = new JLabel("<html><body style ='width:230px'>" + shortDesc + "</body></html>");
        descLabel.setFont(ThemeConstants.fontSmall);
        descLabel.setForeground(ThemeConstants.colorTextSecondary);

        //Metadata row
        String entryText = world.getEntryCount() + " entr" + (world.getEntryCount() == 1 ? "y" : "ies");
        String dateText = "Created: " + world.getCreatedAt().substring(0, 10);
        JLabel metaLabel = new JLabel(entryText + " . " + dateText);
        metaLabel.setFont(ThemeConstants.fontSmall);
        metaLabel.setForeground(ThemeConstants.colorTextPlaceholder);

        JButton deleteButton = buildStyledButton("Delete", ThemeConstants.colorDanger, ThemeConstants.colorDangerDark);
        deleteButton.setFont(ThemeConstants.fontSmall);
        deleteButton.addActionListener(e-> handleDeleteWorld(world));

        //Top section
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(nameLabel);
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(descLabel);

        //Bottom section
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(metaLabel, BorderLayout.CENTER);
        bottomPanel.add(deleteButton, BorderLayout.EAST);

        card.add(topPanel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        //Hover effect
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e){
                card.setBackground(ThemeConstants.colorSurfaceHover);
                card.repaint();
            }
            public void mouseExited(MouseEvent e){
                card.setBackground(ThemeConstants.colorSurface);
                card.repaint();
            }

            public void mouseClicked(MouseEvent e) {
                // Only navigate if the delete button wasn't clicked
                if (!(e.getSource() instanceof JButton)) {
                    mainFrame.navigateToWorld(world.getID());
                }
            }
        });
        return card;
    }

    private JLabel buildEmptyStateLabel(){
        JLabel label = new JLabel(
                "<html><center>No worlds yet.<br>Click '+ New World' to begin.</center></html>",
                SwingConstants.CENTER);
        label.setFont(ThemeConstants.fontBody);
        label.setForeground(ThemeConstants.colorTextSecondary);
        label.setPreferredSize(new Dimension(400, 200));
        return label;
    }

    //Actions
    private void showCreateWorldDialog(){
        JTextField nameField = buildStyledTextField("World name...");

        //Description field
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setFont(ThemeConstants.fontBody);
        descArea.setForeground(ThemeConstants.colorTextPrimary);
        descArea.setBackground(ThemeConstants.colorSurface);
        descArea.setCaretColor(ThemeConstants.colorTextPrimary);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createLineBorder(ThemeConstants.colorBorder));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(null);

        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.setBackground(ThemeConstants.colorSurface);
        dialogPanel.setBorder(new EmptyBorder(ThemeConstants.padding, ThemeConstants.padding, ThemeConstants.padding, ThemeConstants.padding));
        dialogPanel.add(styledLabel("World Name:"));
        dialogPanel.add(Box.createVerticalStrut(4));
        dialogPanel.add(nameField);
        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(styledLabel("Description(optional): "));
        dialogPanel.add(Box.createVerticalStrut(4));
        dialogPanel.add(descScroll);

        int result = JOptionPane.showConfirmDialog(
                mainFrame, dialogPanel, "Create New World", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if(result == JOptionPane.OK_OPTION){
            String name = nameField.getText().trim();
            String description = descArea.getText().trim();

            Validator.ValidationResult result1 = Validator.validateWorld(name, description);
            if (!result1.isValid()) {
                NotificationManager.showWarning(mainFrame, result1.getMessage());
                return;
            }

            try{
                World newWorld = worldManager.createWorld(name, description);
                mainFrame.saveWorld(newWorld);
                NotificationManager.showSuccess(mainFrame, newWorld.getName() + " created!");
                refresh();
            } catch (IllegalArgumentException e){
                NotificationManager.showError(mainFrame, e.getMessage());
            }
        }
    }

    private void handleDeleteWorld(World world){
        boolean confirmed = NotificationManager.showConfirm(
                mainFrame,
                "Delete \"" + world.getName() + "\"?",
                "Confirm Delete");

        if (confirmed) {
            worldManager.deleteWorld(world.getID());
            mainFrame.deleteSavedWorld(world.getID());
            NotificationManager.showSuccess(mainFrame,
                    "\"" + world.getName() + "\" deleted.");
            refresh();
        }
    }

    public void refresh() {
        populateGrid();
    }

    private JButton buildStyledButton(String text, Color bgColor, Color hoverColor){
        JButton button = new JButton(text){
            protected void paintComponent(Graphics g){
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
            public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(bgColor); }
        });

        return button;
    }

    private JTextField buildStyledTextField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(ThemeConstants.fontBody);
        field.setForeground(ThemeConstants.colorTextPrimary);
        field.setBackground(ThemeConstants.colorSurface);
        field.setCaretColor(ThemeConstants.colorTextPrimary);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConstants.colorBorder),
                new EmptyBorder(6, 8, 6, 8)));

        // Placeholder behavior: grey hint text cleared on focus
        field.setText(placeholder);
        field.setForeground(ThemeConstants.colorTextPlaceholder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(ThemeConstants.colorTextPrimary);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(ThemeConstants.colorTextPlaceholder);
                }
            }
        });

        return field;
    }

    private JLabel styledLabel(String text){
        JLabel label = new JLabel(text);
        label.setFont(ThemeConstants.fontSmall);
        label.setForeground(ThemeConstants.colorTextSecondary);
        return label;
    }
}