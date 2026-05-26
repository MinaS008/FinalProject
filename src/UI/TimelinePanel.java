package UI;

import Controller.TimelineManager;
import Controller.WorldManager;
import Helper.ThemeConstants;
import Model.CodexEntry;
import Model.LoreEntry;
import Model.TimelineEvent;
import Model.World;
import Utilities.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;


public class TimelinePanel extends JPanel {
    private static final Color eraMythic = new Color(170, 100, 230);
    private static final Color eraAncient = new Color(190, 140, 60);
    private static final Color eraClassical = new Color(80,  150, 160);
    private static final Color eraMedieval = new Color(80,  120, 180);
    private static final Color eraModern = new Color(70,  160, 100);
    private static final Color eraFuture = new Color(60,  160, 190);
    private static final Color eraUnknown = new Color(110, 110, 120);

    private static final int spineX = 28;
    private static final int dotRadius = 6;

    //Fields
    private final MainFrame mainFrame;
    private final WorldManager worldManager;
    private final TimelineManager timelineManager;

    private World currentWorld;
    private JLabel titleLabel;
    private JPanel timelineBody;
    private JScrollPane scrollPane;

    public TimelinePanel(MainFrame mainFrame, WorldManager worldManager){
        this.mainFrame = mainFrame;
        this.worldManager = worldManager;
        this.timelineManager = new TimelineManager();

        setLayout(new BorderLayout());
        setBackground(ThemeConstants.colorBackground);
        setBorder(new EmptyBorder(
                ThemeConstants.padding*2,
                ThemeConstants.padding*2,
                ThemeConstants.padding*2,
                ThemeConstants.padding*2

        ));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollBody(), BorderLayout.CENTER);

        ThemeManager.getInstance().addChangeListener(()-> {
            if(timelineBody != null) timelineBody.setBackground(ThemeConstants.colorBackground);
            if (currentWorld != null) rebuild();
        });
    }

    public void loadWorld(World world){
        this.currentWorld = world;
        titleLabel.setText(world.getName() + " - Timeline");
        rebuild();
    }

    public void refresh(){
        if(currentWorld != null) rebuild();
    }

    //Header
    private JPanel buildHeader(){
        JButton backButton = buildStyledButton("← Back",
                ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        backButton.setForeground(ThemeConstants.colorTextSecondary);
        backButton.addActionListener(e -> mainFrame.navigateToWorldView());

        titleLabel = new JLabel("Timeline");
        titleLabel.setFont(ThemeConstants.fontTitle);
        titleLabel.setForeground(ThemeConstants.colorAccent);

        JPanel leftBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBlock.setOpaque(false);
        leftBlock.add(backButton);
        leftBlock.add(titleLabel);

        JButton addButton = buildStyledButton("+ Add Event",
                ThemeConstants.colorAccent, ThemeConstants.colorAccentDark);
        addButton.setForeground(ThemeConstants.colorBackground);
        addButton.addActionListener(e -> openEventDialog(null));

        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeConstants.colorBorder);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(leftBlock,  BorderLayout.WEST);
        topRow.add(addButton,  BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, ThemeConstants.padding, 0));
        wrapper.add(topRow, BorderLayout.CENTER);
        wrapper.add(sep,    BorderLayout.SOUTH);
        return wrapper;
    }

    //Scroll body
    private JScrollPane buildScrollBody(){
        timelineBody = new JPanel();
        timelineBody.setLayout(new BoxLayout(timelineBody, BoxLayout.Y_AXIS));
        timelineBody.setBackground(ThemeConstants.colorBackground);

        scrollPane = new JScrollPane(timelineBody);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void rebuild(){
        timelineBody.removeAll();

        List<TimelineEvent> events = timelineManager.getEventsSorted(currentWorld);

        if(events.isEmpty()){
            timelineBody.add(buildEmptyState());
        } else {
            String lastEra = null;
            for(TimelineEvent event : events){
                if (!event.getEra().equals(lastEra)){
                    if(lastEra != null) timelineBody.add(spacer(8));
                    timelineBody.add(buildEraHeading(event.getEra()));
                    timelineBody.add(spacer(6));
                    lastEra = event.getEra();
                }

                timelineBody.add(buildEventCard(event));
                timelineBody.add(spacer(10));
            }
        }

        timelineBody.add(spacer(ThemeConstants.padding*2));
        SwingUtilities.invokeLater(()-> scrollPane.getVerticalScrollBar().setValue(0));
        timelineBody.revalidate();
        timelineBody.repaint();
    }

    private JPanel buildEraHeading(String era){
      Color eraColor = eraColor(era);

      JPanel row = new JPanel(new BorderLayout(8, 0));
      row.setOpaque(false);
      row.setAlignmentX(Component.LEFT_ALIGNMENT);
      row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

      JLabel pill = new JLabel(era.toUpperCase()) {
          protected void paintComponent(Graphics g) {
              Graphics2D g2 = (Graphics2D) g.create();
              g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_ON);
              g2.setColor(new Color(
                      eraColor.getRed(), eraColor.getGreen(), eraColor.getBlue(), 40));
              g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
              g2.dispose();
              super.paintComponent(g);
          }
      };

      pill.setFont(new Font("SansSerif", Font.BOLD, 11));
      pill.setForeground(eraColor);
      pill.setOpaque(false);
      pill.setBorder(new EmptyBorder(3, 10, 3, 10));

      JPanel line = new JPanel(){
          protected void paintComponent(Graphics g){
              super.paintComponent(g);
              Graphics2D g2 = (Graphics2D) g.create();
              g2.setColor(new Color(
                      eraColor.getRed(), eraColor.getGreen(), eraColor.getBlue(), 60));
              int y = getHeight() / 2;
              g2.drawLine(0, y, getWidth(), y);
              g2.dispose();
          }
      };

      line.setOpaque(false);

      row.add(pill, BorderLayout.WEST);
      row.add(line, BorderLayout.CENTER);
      return row;
    }

    private JPanel buildEventCard(TimelineEvent event){
        Color eraColor = eraColor(event.getEra());

        JPanel spine = new JPanel(new BorderLayout(0, 0)) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Vertical line running down the spine
                int cx = spineX;
                g2.setColor(new Color(eraColor.getRed(), eraColor.getGreen(),
                        eraColor.getBlue(), 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(cx, 0, cx, getHeight());

                // Filled circle (dot) at the top
                g2.setColor(eraColor);
                g2.fillOval(cx - dotRadius, ThemeConstants.padding - dotRadius,
                        dotRadius * 2, dotRadius * 2);
                g2.dispose();
            }
        };

        spine.setOpaque(false);
        spine.setAlignmentX(Component.LEFT_ALIGNMENT);
        spine.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Card content sits to the right of the spine
        JPanel card = buildCardContent(event, eraColor);
        card.setBorder(new EmptyBorder(0, spineX + 18, 0, 0));

        spine.add(card, BorderLayout.CENTER);
        return spine;
    }

    private JPanel buildCardContent(TimelineEvent event, Color eraColor){
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeConstants.colorSurface);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                // Left accent border using era colour
                g2.setColor(eraColor);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(0, ThemeConstants.cornerRadius / 2,
                        0, getHeight() - ThemeConstants.cornerRadius / 2);
                // Outer border
                g2.setColor(ThemeConstants.colorBorder);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        ThemeConstants.cornerRadius, ThemeConstants.cornerRadius);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(
                ThemeConstants.padding, ThemeConstants.padding + 6,
                ThemeConstants.padding, ThemeConstants.padding));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        //Title row
        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel titleLbl = new JLabel(event.getTitle());
        titleLbl.setFont(ThemeConstants.fontHeading);
        titleLbl.setForeground(ThemeConstants.colorTextPrimary);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionRow.setOpaque(false);

        JButton editBtn = buildSmallButton("✎ Edit",
                ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        editBtn.setForeground(ThemeConstants.colorTextSecondary);
        editBtn.addActionListener(e -> openEventDialog(event));

        JButton deleteBtn = buildSmallButton("✕",
                ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        deleteBtn.setForeground(ThemeConstants.colorDanger);
        deleteBtn.addActionListener(e -> handleDelete(event));

        actionRow.add(editBtn);
        actionRow.add(deleteBtn);

        titleRow.add(titleLbl,  BorderLayout.WEST);
        titleRow.add(actionRow, BorderLayout.EAST);
        card.add(titleRow);

        //Date label
        if(event.getDateLabel() != null && !event.getDateLabel().isBlank()){
            card.add(spacer(4));
            JLabel dateLbl = new JLabel("🕐  " + event.getDateLabel());
            dateLbl.setFont(ThemeConstants.fontSmall);
            dateLbl.setForeground(eraColor);
            dateLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(dateLbl);
        }

        //Description
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            card.add(spacer(8));
            JTextArea descArea = buildReadonlyTextArea(event.getDescription());
            card.add(descArea);
        }

        //Involved entities
        List<String> entities = event.getInvolvedEntities();
        if (!entities.isEmpty()) {
            card.add(spacer(8));
            card.add(buildChipRow("Involved:", entities, eraColor));
        }

        //Consequences
        List<String> consequences = event.getConsequences();
        if (!consequences.isEmpty()) {
            card.add(spacer(6));
            card.add(buildChipRow("Consequences:", consequences,
                    new Color(180, 80, 80)));
        }

        return card;
    }

    //Chip row (compact list of tags)
    private JPanel buildChipRow(String label, List<String> items, Color chipColor){
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeConstants.fontSmall);
        lbl.setForeground(ThemeConstants.colorTextSecondary);
        row.add(lbl);

        for (String item : items) {
            JLabel chip = new JLabel(item) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(chipColor.getRed(), chipColor.getGreen(),
                            chipColor.getBlue(), 35));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            chip.setFont(ThemeConstants.fontSmall);
            chip.setForeground(chipColor);
            chip.setOpaque(false);
            chip.setBorder(new EmptyBorder(2, 8, 2, 8));
            row.add(chip);
        }

        return row;
    }

    //Read-only text area (wraps description)
    private JTextArea buildReadonlyTextArea(String text){
        JTextArea area = new JTextArea(text);
        area.setFont(ThemeConstants.fontBody);
        area.setForeground(ThemeConstants.colorTextSecondary);
        area.setBackground(ThemeConstants.colorSurface);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        area.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        area.setBorder(null);
        return area;
    }

    private JPanel buildEmptyState(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(ThemeConstants.padding*3, 0, 0, 0));

        JLabel icon = new JLabel("⟳");
        icon.setFont(new Font("Serif", Font.PLAIN, 48));
        icon.setForeground(ThemeConstants.colorTextPlaceholder);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("No events yet - start writing history");
        msg.setFont(ThemeConstants.fontBody);
        msg.setForeground(ThemeConstants.colorTextPlaceholder);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Click '+ Add Event' to create your first timeline entry.");
        hint.setFont(ThemeConstants.fontSmall);
        hint.setForeground(ThemeConstants.colorTextPlaceholder);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(spacer(12));
        panel.add(msg);
        panel.add(spacer(6));
        panel.add(hint);
        return panel;
    }

    private void openEventDialog(TimelineEvent existingEvent){
        boolean isNew = (existingEvent == null);
        String dialogTitle = isNew ? "Add Timeline Event" : "Edit Event";

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                dialogTitle, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 560);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(ThemeConstants.colorBackground);
        dialog.getContentPane().setLayout(new BorderLayout());

        //Dialog header
        JLabel dHeader = new JLabel("  " + dialogTitle);
        dHeader.setFont(ThemeConstants.fontHeading);
        dHeader.setForeground(ThemeConstants.colorAccent);
        dHeader.setOpaque(true);
        dHeader.setBackground(ThemeConstants.colorSurface);
        dHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.colorBorder),
                new EmptyBorder(14, 16, 14, 16)));
        dialog.getContentPane().add(dHeader, BorderLayout.NORTH);

        //Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(ThemeConstants.colorBackground);
        form.setBorder(new EmptyBorder(16, 20, 8, 20));

        //Title
        JTextField titleField = buildFormField(isNew ? "" : existingEvent.getTitle());
        form.add(buildFieldLabel("Event Title *"));
        form.add(spacer(4));
        form.add(titleField);
        form.add(spacer(12));

        // Date label
        JTextField dateField = buildFormField(
                isNew ? "" : (existingEvent.getDateLabel() != null ? existingEvent.getDateLabel() : ""));
        form.add(buildFieldLabel("In-World Date / Year  (e.g. \"Year 432 of the Third Age\")"));
        form.add(spacer(4));
        form.add(dateField);
        form.add(spacer(12));

        // Sort order
        int defaultOrder = isNew
                ? timelineManager.nextSortOrder(currentWorld)
                : existingEvent.getSortOrder();
        JTextField orderField = buildFormField(String.valueOf(defaultOrder));
        form.add(buildFieldLabel("Sort Order  (lower = earlier on timeline)"));
        form.add(spacer(4));
        form.add(orderField);
        form.add(spacer(12));

        // Era
        form.add(buildFieldLabel("Era *"));
        form.add(spacer(4));
        JComboBox<String> eraCombo = new JComboBox<>(TimelineManager.ERAS);
        eraCombo.setFont(ThemeConstants.fontBody);
        eraCombo.setBackground(ThemeConstants.colorSurface);
        eraCombo.setForeground(ThemeConstants.colorTextPrimary);
        eraCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        eraCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (!isNew) eraCombo.setSelectedItem(existingEvent.getEra());
        form.add(eraCombo);
        form.add(spacer(12));

        // Description
        form.add(buildFieldLabel("Description"));
        form.add(spacer(4));
        JTextArea descArea = new JTextArea(isNew ? "" : existingEvent.getDescription(), 4, 30);
        descArea.setFont(ThemeConstants.fontBody);
        descArea.setForeground(ThemeConstants.colorTextPrimary);
        descArea.setBackground(ThemeConstants.colorSurface);
        descArea.setCaretColor(ThemeConstants.colorTextPrimary);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setBorder(BorderFactory.createLineBorder(ThemeConstants.colorBorder));
        form.add(descScroll);
        form.add(spacer(12));

        // Involved entities
        form.add(buildFieldLabel("Involved Entities  (comma-separated)"));
        form.add(spacer(4));
        String entitiesDefault = isNew ? "" : String.join(", ", existingEvent.getInvolvedEntities());
        JTextField entitiesField = buildFormField(entitiesDefault);
        form.add(entitiesField);
        form.add(spacer(12));

        // Consequences
        form.add(buildFieldLabel("Consequences  (comma-separated)"));
        form.add(spacer(4));
        String consDefault = isNew ? "" : String.join(", ", existingEvent.getConsequences());
        JTextField consField = buildFormField(consDefault);
        form.add(consField);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dialog.getContentPane().add(formScroll, BorderLayout.CENTER);

        //Footer
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeConstants.fontSmall);
        errorLabel.setForeground(ThemeConstants.colorDanger);

        JButton cancelBtn = buildDialogButton("Cancel",
                ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover,
                ThemeConstants.colorTextSecondary);
        JButton saveBtn   = buildDialogButton(isNew ? "Add Event" : "Save Changes",
                ThemeConstants.colorAccent, ThemeConstants.colorAccentDark,
                ThemeConstants.colorBackground);

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String date = dateField.getText().trim();
            String description = descArea.getText().trim();
            String era = (String) eraCombo.getSelectedItem();
            String entitiesRaw = entitiesField.getText().trim();
            String consRaw = consField.getText().trim();

            int order;
            try {
                order = Integer.parseInt(orderField.getText().trim());
            } catch (NumberFormatException ex) {
                errorLabel.setText("Sort order must be a whole number.");
                return;
            }

            Validator.ValidationResult vr = Validator.validateEntryBase(title, description);
            if (!vr.isValid()) {
                errorLabel.setText(vr.getMessage());
                return;
            }

            try {
                if (isNew) {
                    TimelineEvent created = timelineManager.createEvent(
                            currentWorld, title, description, era, date, order);
                    // Add comma-separated entities and consequences
                    addCommaSeparated(created, entitiesRaw, consRaw);
                } else {
                    timelineManager.updateEvent(currentWorld, existingEvent.getId(),
                            title, description, era, date, order);
                    // Rebuild lists: clear then re-add from fields
                    rebuildLists(existingEvent, entitiesRaw, consRaw);
                }
                mainFrame.saveWorld(currentWorld);
                dialog.dispose();
                rebuild();
            } catch (IllegalArgumentException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setBackground(ThemeConstants.colorSurface);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeConstants.colorBorder),
                new EmptyBorder(10, 16, 10, 16)));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        footer.add(errorLabel, BorderLayout.WEST);
        footer.add(btnPanel,   BorderLayout.EAST);
        dialog.getContentPane().add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void handleDelete(TimelineEvent event){
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete \"" + event.getTitle() + "\"?\nThis cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            timelineManager.deleteEvent(currentWorld, event.getId());
            mainFrame.saveWorld(currentWorld);
            rebuild();
        }
    }

    //Form helpers
    private JTextField buildFormField(String initial){
        JTextField field = new JTextField(initial);
        field.setFont(ThemeConstants.fontBody);
        field.setForeground(ThemeConstants.colorTextPrimary);
        field.setBackground(ThemeConstants.colorSurface);
        field.setCaretColor(ThemeConstants.colorTextPrimary);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ThemeConstants.colorBorder), new EmptyBorder(6, 8, 6, 8)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JLabel buildFieldLabel(String text){
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeConstants.fontSmall);
        lbl.setForeground(ThemeConstants.colorTextSecondary);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton buildDialogButton(String text, Color bg, Color hover, Color fg){
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
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
        btn.setPreferredSize(new Dimension(130, 34));
        return btn;
    }

    //List helpers
    private void addCommaSeparated(TimelineEvent event, String entitiesRaw, String consRaw){
        if(!entitiesRaw.isBlank()){
            for(String s: entitiesRaw.split(",")){
                String trimmed = s.trim();
                if(!trimmed.isEmpty()) event.addInvolvedEntity(trimmed);
            }
        }

        if(!consRaw.isBlank()){
            for(String s: consRaw.split(",")){
                String trimmed = s.trim();
                if(!trimmed.isEmpty()) event.addConsequence(trimmed);
            }
        }
    }

    private void rebuildLists(TimelineEvent event, String entitiesRaw, String consRaw){
        for (String e : event.getInvolvedEntities()) event.removeInvolvedEntity(e);
        for (String c : event.getConsequences()) event.removeConsequence(c);
        addCommaSeparated(event, entitiesRaw, consRaw);
    }

    private JButton buildStyledButton(String text, Color bg, Color hover){
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
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
        button.setForeground(ThemeConstants.colorTextPrimary);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(6, 14, 6, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(hover); button.repaint(); }
            public void mouseExited(MouseEvent e)  { button.setBackground(bg);    button.repaint(); }
        });
        return button;
    }

    private JButton buildSmallButton(String text, Color bg, Color hover){
        JButton btn = buildStyledButton(text, bg, hover);
        btn.setFont(ThemeConstants.fontSmall);
        btn.setBorder(new EmptyBorder(4, 10, 4, 10));
        return btn;
    }

    //Utility
    private Component spacer(int h){
        JPanel sp = new JPanel();
        sp.setOpaque(false);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        sp.setPreferredSize(new Dimension(0, h));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sp;
    }

    private static Color eraColor(String era){
        if (era == null) return eraUnknown;
        switch (era) {
            case LoreEntry.eraMythic: return eraMythic;
            case LoreEntry.eraAncient: return eraAncient;
            case LoreEntry.eraClassical: return eraClassical;
            case LoreEntry.eraMedieval: return eraMedieval;
            case LoreEntry.eraModern: return eraModern;
            case LoreEntry.eraFuture: return eraFuture;
            default: return eraUnknown;
        }
    }
}
