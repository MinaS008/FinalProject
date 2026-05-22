package UI;
import Model.*;
import Utilities.*;
import Persistence.*;
import Helper.*;
import Controller.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class EntryEditorPanel extends JPanel {
    private static final String[] entryTypes = {
            "Character", "Location", "Item", "Faction", "Lore"
    };

    private final MainFrame mainFrame;
    private final WorldManager worldManager;
    private World currentWorld;

    private CodexEntry entryToEdit;
    private String selectedType = "Character";
    private JTextField nameField;
    private JTextArea descriptionArea;

    //Character
    private JTextField charRoleField;
    private JTextArea charBackstoryArea;
    private JTextField charAffiliationField;
    private JTextField charAbilityField;
    private JTextField charRelationshipField;
    private JPanel charAffiliationListPanel;
    private JPanel charAbilityListPanel;
    private JPanel charRelationshipListPanel;
    private List<String> charAffiliations = new ArrayList<>();
    private List<String> charAbilities = new ArrayList<>();
    private List<String> charRelationships = new ArrayList<>();

    //Location
    private JTextField locationTypeField;
    private JTextField locationRegionField;
    private JTextField subLocationField;
    private JTextField locationConnectionField;
    private JPanel subLocationListPanel;
    private JPanel locationConnectionListPanel;
    private List<String> subLocations = new ArrayList<>();
    private List<String> locationConnections = new ArrayList<>();

    //Item
    private JComboBox<String> itemRarityCombo;
    private JTextField itemTypeField;
    private JTextArea itemPowerArea;
    private JTextField itemOwnerField;
    private JPanel itemOwnerListPanel;
    private List<String> itemOwners = new ArrayList<>();

    //Faction
    private JTextArea  factionGoalArea;
    private JTextArea  factionIdeologyArea;
    private JTextField factionMemberField;
    private JTextField factionRelationshipField;
    private JPanel factionMemberListPanel;
    private JPanel factionRelationshipListPanel;
    private List<String> factionMembers = new ArrayList<>();
    private List<String> factionRelationships = new ArrayList<>();

    //Lore Entry
    private JComboBox<String> loreEraCombo;
    private JTextField loreTimelineField;
    private JTextField loreConsequenceField;
    private JTextField loreReferenceField;
    private JPanel loreTimelineListPanel;
    private JPanel loreConsequenceListPanel;
    private JPanel loreReferenceListPanel;
    private List<String> loreTimeline = new ArrayList<>();
    private List<String> loreConsequences = new ArrayList<>();
    private List<String> loreReferences = new ArrayList<>();

    //Panels
    private JLabel modeTitle;
    private JPanel typeSelectorRow;
    private JPanel typeSpecificForm;
    private JPanel formContainer;
    private JScrollPane scrollPane;

    public EntryEditorPanel(MainFrame mainFrame, WorldManager worldManager) {
        this.mainFrame = mainFrame;
        this.worldManager = worldManager;

        setLayout(new BorderLayout());
        setBackground(ThemeConstants.colorBackground);
        setBorder(new EmptyBorder(
                ThemeConstants.padding * 2, ThemeConstants.padding * 2,
                ThemeConstants.padding * 2, ThemeConstants.padding * 2));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollForm(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        modeTitle = new JLabel("New Entry");
        modeTitle.setFont(ThemeConstants.fontTitle);
        modeTitle.setForeground(ThemeConstants.colorAccent);

        JButton backButton = buildStyledButton("← Back",
                ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        backButton.setForeground(ThemeConstants.colorTextSecondary);
        backButton.addActionListener(e -> handleCancel());

        JButton saveButton = buildStyledButton("✓ Save Entry",
                ThemeConstants.colorAccent, ThemeConstants.colorAccentDark);
        saveButton.addActionListener(e -> handleSave());

        JButton cancelButton = buildStyledButton("✕ Cancel",
                ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        cancelButton.setForeground(ThemeConstants.colorTextSecondary);
        cancelButton.addActionListener(e -> handleCancel());

        JPanel leftBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBlock.setOpaque(false);
        leftBlock.add(backButton);
        leftBlock.add(modeTitle);

        JPanel rightBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBlock.setOpaque(false);
        rightBlock.add(cancelButton);
        rightBlock.add(saveButton);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(leftBlock, BorderLayout.WEST);
        row.add(rightBlock, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeConstants.colorBorder);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, ThemeConstants.padding, 0));
        wrapper.add(row, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);
        return wrapper;
    }

    private JScrollPane buildScrollForm(){
        formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(ThemeConstants.colorBackground);
        formContainer.setBorder(new EmptyBorder(ThemeConstants.padding, 0, 0, 0));

        scrollPane = new JScrollPane(formContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    //Public API
    public void loadForCreate(World world){
        this.currentWorld = world;
        this.entryToEdit = null;
        this.selectedType = "Character";

        modeTitle.setText("New Entry");
        resetAllLists();
        rebuildForm(true);
    }

    public void loadForEdit(World world, CodexEntry entry){
        this.currentWorld = world;
        this.entryToEdit = entry;
        this.selectedType = entry.getType();

        modeTitle.setText("Edit Entry");
        resetAllLists();
        prefillListsFrom(entry);
        rebuildForm(false);
    }

    //Form Building
    private void rebuildForm(boolean showTypeSelector){
        formContainer.removeAll();

        if(showTypeSelector){
            formContainer.add(buildTypeSelectorRow());
            formContainer.add(vertSpacer(ThemeConstants.padding));
        }

        formContainer.add(buildSharedFields());
        formContainer.add(vertSpacer(ThemeConstants.padding));

        typeSpecificForm = buildTypeSpecificFields(selectedType);
        formContainer.add(typeSpecificForm);
        formContainer.add(vertSpacer(ThemeConstants.padding*2));

        if(entryToEdit != null){
            nameField.setText(entryToEdit.getName());
            descriptionArea.setText(entryToEdit.getDescription());
        }

        SwingUtilities.invokeLater(()->
                scrollPane.getVerticalScrollBar().setValue(0));

        formContainer.revalidate();
        formContainer.repaint();
    }

    private JPanel buildTypeSelectorRow(){
        JPanel section = buildSection("Entry Type");

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        for(String type: entryTypes){
            boolean active = type.equals(selectedType);
            JButton typeButton = buildTypeToggleButton(type, active);
            typeButton.addActionListener(e->{
                selectedType = type;
                rebuildForm(true);
            });
            buttonRow.add(typeButton);
        }

        section.add(buttonRow);
        return section;
    }

    private JButton buildTypeToggleButton(String type, boolean active){
        JButton button = new JButton(type){
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

        if(active) {
            button.setBackground(ThemeConstants.colorAccent);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(ThemeConstants.colorSurface);
            button.setForeground(ThemeConstants.colorTextSecondary);
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){
                    button.setBackground(ThemeConstants.colorSurfaceHover);
                    button.repaint();
                }
                public void mouseExited(MouseEvent e){
                    button.setBackground(ThemeConstants.colorSurface);
                    button.repaint();
                }
            });
        }
        return button;
    }

    private JPanel buildSharedFields(){
        JPanel section = buildSection("Basic Info");

        section.add(formLabel("Name *"));
        section.add(vertSpacer(4));
        nameField = buildTextField("Enter a name...");
        section.add(nameField);
        section.add(vertSpacer(ThemeConstants.padding));

        section.add(formLabel("Description"));
        section.add(vertSpacer(4));
        descriptionArea = buildTextArea("Enter a description...", 4);
        section.add(new JScrollPane(descriptionArea) {{
            setBorder(BorderFactory.createLineBorder(ThemeConstants.colorBorder));
            setOpaque(false);
            getViewport().setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        }});

        return section;
    }

    private JPanel buildTypeSpecificFields(String type){
        switch (type){
            case "Character": return buildCharacterFields();
            case "Location": return buildLocationFields();
            case "Item": return buildItemFields();
            case "Faction": return buildFactionFields();
            case "Lore": return buildLoreFields();
            default: return new JPanel();
        }
    }

    //Type-specific field builders
    private JPanel buildCharacterFields(){
        JPanel section = buildSection("Character Details");

        section.add(formLabel("Role *"));
        section.add(vertSpacer(4));
        charRoleField = buildTextField("e.g. Knight, Merchant, Villain...");
        if (entryToEdit instanceof Model.Character)
            charRoleField.setText(((Model.Character) entryToEdit).getRole());
        section.add(charRoleField);
        section.add(vertSpacer(ThemeConstants.padding));

        section.add(formLabel("Backstory"));
        section.add(vertSpacer(4));
        charBackstoryArea = buildTextArea("Character's origin and history...", 4);
        if (entryToEdit instanceof Model.Character)
            charBackstoryArea.setText(((Model.Character) entryToEdit).getBackstory());
        section.add(scrollWrap(charBackstoryArea, 120));
        section.add(vertSpacer(ThemeConstants.padding));

        // Affiliations list
        charAffiliationListPanel = new JPanel();
        charAffiliationListPanel.setLayout(new BoxLayout(charAffiliationListPanel, BoxLayout.Y_AXIS));
        charAffiliationListPanel.setOpaque(false);
        charAffiliationListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        charAffiliationField = buildTextField("Faction name...");
        section.add(buildTagSection("Affiliations", charAffiliationField,
                charAffiliationListPanel, charAffiliations));
        section.add(vertSpacer(ThemeConstants.padding));

        // Abilities list
        charAbilityListPanel = new JPanel();
        charAbilityListPanel.setLayout(new BoxLayout(charAbilityListPanel, BoxLayout.Y_AXIS));
        charAbilityListPanel.setOpaque(false);
        charAbilityListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        charAbilityField = buildTextField("e.g. Swordsmanship, Telepathy...");
        section.add(buildTagSection("Abilities", charAbilityField,
                charAbilityListPanel, charAbilities));
        section.add(vertSpacer(ThemeConstants.padding));

        // Relationships list
        charRelationshipListPanel = new JPanel();
        charRelationshipListPanel.setLayout(new BoxLayout(charRelationshipListPanel, BoxLayout.Y_AXIS));
        charRelationshipListPanel.setOpaque(false);
        charRelationshipListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        charRelationshipField = buildTextField("e.g. Rival of Aldric...");
        section.add(buildTagSection("Relationships", charRelationshipField,
                charRelationshipListPanel, charRelationships));

        refreshTagPanel(charAffiliationListPanel, charAffiliations);
        refreshTagPanel(charAbilityListPanel, charAbilities);
        refreshTagPanel(charRelationshipListPanel, charRelationships);

        return section;
    }

    private JPanel buildLocationFields() {
        JPanel section = buildSection("Location Details");

        section.add(formLabel("Location Type *"));
        section.add(vertSpacer(4));
        locationTypeField = buildTextField("e.g. City, Kingdom, Planet, Dungeon...");
        if (entryToEdit instanceof Location)
            locationTypeField.setText(((Location) entryToEdit).getLocationType());
        section.add(locationTypeField);
        section.add(vertSpacer(ThemeConstants.padding));

        section.add(formLabel("Region"));
        section.add(vertSpacer(4));
        locationRegionField = buildTextField("e.g. Northern Wastes, Outer Rim...");
        if (entryToEdit instanceof Location)
            locationRegionField.setText(((Location) entryToEdit).getRegion());
        section.add(locationRegionField);
        section.add(vertSpacer(ThemeConstants.padding));

        subLocationListPanel = new JPanel();
        subLocationListPanel.setLayout(new BoxLayout(subLocationListPanel, BoxLayout.Y_AXIS));
        subLocationListPanel.setOpaque(false);
        subLocationListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subLocationField = buildTextField("Sub-location name...");
        section.add(buildTagSection("Sub-locations", subLocationField,
                subLocationListPanel, subLocations));
        section.add(vertSpacer(ThemeConstants.padding));

        locationConnectionListPanel = new JPanel();
        locationConnectionListPanel.setLayout(new BoxLayout(locationConnectionListPanel, BoxLayout.Y_AXIS));
        locationConnectionListPanel.setOpaque(false);
        locationConnectionListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationConnectionField = buildTextField("e.g. Home of Aldric...");
        section.add(buildTagSection("Connections", locationConnectionField,
                locationConnectionListPanel, locationConnections));

        refreshTagPanel(subLocationListPanel, subLocations);
        refreshTagPanel(locationConnectionListPanel, locationConnections);

        return section;
    }

    private JPanel buildItemFields(){
        JPanel section = buildSection("Item Details");

        String[] rarities = {Item.rarityCommon, Item.rarityUncommon,
                Item.rarityRare, Item.rarityEpic, Item.rarityLegendary};

        section.add(formLabel("Item Type *"));
        section.add(vertSpacer(4));
        itemTypeField = buildTextField("e.g. Weapon, Artifact, Tome, Potion...");
        if (entryToEdit instanceof Item)
            itemTypeField.setText(((Item) entryToEdit).getItemType());
        section.add(itemTypeField);
        section.add(vertSpacer(ThemeConstants.padding));

        section.add(formLabel("Rarity"));
        section.add(vertSpacer(4));
        itemRarityCombo = new JComboBox<>(rarities);
        itemRarityCombo.setFont(ThemeConstants.fontBody);
        itemRarityCombo.setBackground(ThemeConstants.colorSurface);
        itemRarityCombo.setForeground(ThemeConstants.colorTextPrimary);
        itemRarityCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemRarityCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (entryToEdit instanceof Item)
            itemRarityCombo.setSelectedItem(((Item) entryToEdit).getRarity());
        section.add(itemRarityCombo);
        section.add(vertSpacer(ThemeConstants.padding));

        section.add(formLabel("Power / Special Ability"));
        section.add(vertSpacer(4));
        itemPowerArea = buildTextArea("Describe the item's power or effect...", 3);
        if (entryToEdit instanceof Item)
            itemPowerArea.setText(((Item) entryToEdit).getPower());
        section.add(scrollWrap(itemPowerArea, 100));
        section.add(vertSpacer(ThemeConstants.padding));

        itemOwnerListPanel = new JPanel();
        itemOwnerListPanel.setLayout(new BoxLayout(itemOwnerListPanel, BoxLayout.Y_AXIS));
        itemOwnerListPanel.setOpaque(false);
        itemOwnerListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemOwnerField = buildTextField("Owner name (add in chronological order)...");
        section.add(buildTagSection("Ownership History", itemOwnerField,
                itemOwnerListPanel, itemOwners));

        refreshTagPanel(itemOwnerListPanel, itemOwners);

        return section;
    }

    private JPanel buildFactionFields(){
        JPanel section = buildSection("Faction Details");

        section.add(formLabel("Goal *"));
        section.add(vertSpacer(4));
        factionGoalArea = buildTextArea("The faction's primary objective...", 3);
        if (entryToEdit instanceof Faction)
            factionGoalArea.setText(((Faction) entryToEdit).getGoal());
        section.add(scrollWrap(factionGoalArea, 100));
        section.add(vertSpacer(ThemeConstants.padding));

        section.add(formLabel("Ideology"));
        section.add(vertSpacer(4));
        factionIdeologyArea = buildTextArea("The faction's beliefs and values...", 3);
        if (entryToEdit instanceof Faction)
            factionIdeologyArea.setText(((Faction) entryToEdit).getIdeology());
        section.add(scrollWrap(factionIdeologyArea, 100));
        section.add(vertSpacer(ThemeConstants.padding));

        factionMemberListPanel = new JPanel();
        factionMemberListPanel.setLayout(new BoxLayout(factionMemberListPanel, BoxLayout.Y_AXIS));
        factionMemberListPanel.setOpaque(false);
        factionMemberListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        factionMemberField = buildTextField("Member name...");
        section.add(buildTagSection("Members", factionMemberField,
                factionMemberListPanel, factionMembers));
        section.add(vertSpacer(ThemeConstants.padding));

        factionRelationshipListPanel = new JPanel();
        factionRelationshipListPanel.setLayout(new BoxLayout(factionRelationshipListPanel, BoxLayout.Y_AXIS));
        factionRelationshipListPanel.setOpaque(false);
        factionRelationshipListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        factionRelationshipField = buildTextField("e.g. Allied with Silver Conclave...");
        section.add(buildTagSection("Faction Relationships", factionRelationshipField,
                factionRelationshipListPanel, factionRelationships));

        refreshTagPanel(factionMemberListPanel, factionMembers);
        refreshTagPanel(factionRelationshipListPanel, factionRelationships);

        return section;

    }

    private JPanel buildLoreFields() {
        JPanel section = buildSection("Lore Details");

        String[] eras = {LoreEntry.eraAncient, LoreEntry.eraClassical, LoreEntry.eraMedieval,
                LoreEntry.eraModern, LoreEntry.eraFuture, LoreEntry.eraMythic, LoreEntry.eraUnknown};

        section.add(formLabel("Era"));
        section.add(vertSpacer(4));
        loreEraCombo = new JComboBox<>(eras);
        loreEraCombo.setFont(ThemeConstants.fontBody);
        loreEraCombo.setBackground(ThemeConstants.colorSurface);
        loreEraCombo.setForeground(ThemeConstants.colorTextPrimary);
        loreEraCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        loreEraCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (entryToEdit instanceof LoreEntry)
            loreEraCombo.setSelectedItem(((LoreEntry) entryToEdit).getEra());
        section.add(loreEraCombo);
        section.add(vertSpacer(ThemeConstants.padding));

        loreTimelineListPanel = new JPanel();
        loreTimelineListPanel.setLayout(new BoxLayout(loreTimelineListPanel, BoxLayout.Y_AXIS));
        loreTimelineListPanel.setOpaque(false);
        loreTimelineListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loreTimelineField = buildTextField("e.g. Year 412 — The siege begins...");
        section.add(buildTagSection("Timeline", loreTimelineField,
                loreTimelineListPanel, loreTimeline));
        section.add(vertSpacer(ThemeConstants.padding));

        loreConsequenceListPanel = new JPanel();
        loreConsequenceListPanel.setLayout(new BoxLayout(loreConsequenceListPanel, BoxLayout.Y_AXIS));
        loreConsequenceListPanel.setOpaque(false);
        loreConsequenceListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loreConsequenceField = buildTextField("What changed because of this event...");
        section.add(buildTagSection("Consequences", loreConsequenceField,
                loreConsequenceListPanel, loreConsequences));
        section.add(vertSpacer(ThemeConstants.padding));

        loreReferenceListPanel = new JPanel();
        loreReferenceListPanel.setLayout(new BoxLayout(loreReferenceListPanel, BoxLayout.Y_AXIS));
        loreReferenceListPanel.setOpaque(false);
        loreReferenceListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loreReferenceField = buildTextField("Name of related entry...");
        section.add(buildTagSection("References", loreReferenceField,
                loreReferenceListPanel, loreReferences));

        refreshTagPanel(loreTimelineListPanel, loreTimeline);
        refreshTagPanel(loreConsequenceListPanel, loreConsequences);
        refreshTagPanel(loreReferenceListPanel, loreReferences);

        return section;
    }

    private JPanel buildTagSection(String label, JTextField inputField, JPanel listPanel, List<String> backingList){
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        group.add(formLabel(label));
        group.add(vertSpacer(4));

        //Input row - text field + add button
        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        inputRow.setOpaque(false);
        inputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton addButton = buildStyledButton(" + Add", ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        addButton.setForeground(ThemeConstants.colorAccent);
        addButton.addActionListener(e-> {
            String value = inputField.getText().trim();
            if(!value.isEmpty()){
                backingList.add(value);
                inputField.setText("");
                refreshTagPanel(listPanel, backingList);
            }
        });

        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(addButton, BorderLayout.EAST);

        group.add(inputRow);
        group.add(vertSpacer(6));
        group.add(listPanel);

        return group;
    }

    private void refreshTagPanel(JPanel listPanel, List<String> backingList){
        listPanel.removeAll();
        for(int i = 0; i < backingList.size(); i++){
            final int index = i;
            String item = backingList.get(i);

            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JLabel itemLabel = new JLabel("● " + item);
            itemLabel.setFont(ThemeConstants.fontBody);
            itemLabel.setForeground(ThemeConstants.colorTextPrimary);

            JButton removeButton = new JButton("X");
            removeButton.setFont(ThemeConstants.fontSmall);
            removeButton.setForeground(ThemeConstants.colorDanger);
            removeButton.setBackground(null);
            removeButton.setBorder(null);
            removeButton.setContentAreaFilled(false);
            removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            removeButton.addActionListener(e-> {
                backingList.remove(index);
                refreshTagPanel(listPanel, backingList);
            });

            row.add(itemLabel, BorderLayout.CENTER);
            row.add(removeButton, BorderLayout.EAST);
            listPanel.add(row);
        }
        listPanel.revalidate();
    }

    private void handleSave(){
        String rawName = nameField.getText().trim();
        String name = rawName.equals("Enter a name...") ? "" : rawName;

        String rawDesc = descriptionArea.getText().trim();
        String description = rawDesc.equals("Enter a description...") ? "" : rawDesc;

        Validator.ValidationResult baseResult = Validator.validateEntryBase(name, description);
        if(!baseResult.isValid()){
            NotificationManager.showWarning(mainFrame, baseResult.getMessage());
            return;
        }

        if(entryToEdit == null){
            //CREATE MODE - build the correct subclass and add it
            CodexEntry newEntry = buildNewEntry(name, description);
            if(newEntry == null) return;

            worldManager.addEntry(currentWorld.getID(), newEntry);
            mainFrame.saveWorld(currentWorld);
            mainFrame.navigateToEntry(currentWorld, newEntry);
        } else {
            //EDIT Mode - update base fields via WorldManager, then subclass fields directly
            worldManager.updateEntry(currentWorld.getID(), entryToEdit.getID(), name, description);
            updateSubclassFields(entryToEdit);
            mainFrame.saveWorld(currentWorld);
            mainFrame.navigateToEntry(currentWorld, entryToEdit);
        }
    }

    private CodexEntry buildNewEntry(String name, String description){
        String id = worldManager.generateNewID();
        switch (selectedType) {
            case "Character": {
                String rawRole = charRoleField.getText().trim();
                String role = rawRole.equals("e.g. Knight, Merchant, Villain...") ? "" : rawRole;
                Validator.ValidationResult charResult = Validator.validateCharacter(role);
                if(!charResult.isValid()){
                    showValidationError(charResult.getMessage());
                    return null;
                }
                Model.Character c = new Model.Character(id, name, description, role);
                c.setBackstory(charBackstoryArea.getText().trim());
                charAffiliations.forEach(c::addAffiliation);
                charAbilities.forEach(c::addAbility);
                charRelationships.forEach(c::addRelationship);
                return c;
            }
            case "Location": {
                String rawLocType = locationTypeField.getText().trim();
                String locType = rawLocType.equals("e.g. City, Kingdom, Planet, Dungeon...") ? "" : rawLocType;
                Validator.ValidationResult locResult = Validator.validateLocation(locType);
                if (!locResult.isValid()) {
                    showValidationError(locResult.getMessage());
                    return null;
                }
                Location loc = new Location(id, name, description, locType);
                loc.setRegion(locationRegionField.getText().trim());
                subLocations.forEach(loc::addSubLocation);
                locationConnections.forEach(loc::addConnection);
                return loc;
            }
            case "Item": {
                String rawItemType = itemTypeField.getText().trim();
                String itemType = rawItemType.equals("e.g. Weapon, Artifact, Tome, Potion...") ? "" : rawItemType;
                Validator.ValidationResult itemResult = Validator.validateItem(itemType);
                if (!itemResult.isValid()) {
                    showValidationError(itemResult.getMessage());
                    return null;
                }
                String rarity = (String) itemRarityCombo.getSelectedItem();
                Item item = new Item(id, name, description, rarity, itemType);
                item.setPower(itemPowerArea.getText().trim());
                itemOwners.forEach(item::addOwner);
                return item;
            }
            case "Faction": {
                String goal = factionGoalArea.getText().trim();
                Validator.ValidationResult factionResult = Validator.validateFaction(goal);
                if (!factionResult.isValid()) {
                    showValidationError(factionResult.getMessage());
                    return null;
                }
                Faction f = new Faction(id, name, description, goal);
                f.setIdeology(factionIdeologyArea.getText().trim());
                factionMembers.forEach(f::addMember);
                factionRelationships.forEach(f::addFactionRelationship);
                return f;
            }
            case "Lore": {
                String era = (String) loreEraCombo.getSelectedItem();
                LoreEntry lore = new LoreEntry(id, name, description, era);
                loreTimeline.forEach(lore::addTimelineEntry);
                loreConsequences.forEach(lore::addConsequence);
                loreReferences.forEach(lore::addReference);
                return lore;
            }
            default: return null;
        }
    }

    private void updateSubclassFields(CodexEntry entry){
        switch (entry.getType()) {
            case "Character": {
                Model.Character c = (Model.Character) entry;
                c.setRole(charRoleField.getText().trim());
                c.setBackstory(charBackstoryArea.getText().trim());
                // Replace all list content by clearing and re-adding
                c.getAffiliations().forEach(c::removeAffiliation);
                charAffiliations.forEach(c::addAffiliation);
                c.getAbilities().forEach(c::removeAbility);
                charAbilities.forEach(c::addAbility);
                c.getRelationships().forEach(c::removeRelationship);
                charRelationships.forEach(c::addRelationship);
                break;
            }
            case "Location": {
                Location loc = (Location) entry;
                loc.setLocationType(locationTypeField.getText().trim());
                loc.setRegion(locationRegionField.getText().trim());
                new ArrayList<>(loc.getSubLocations()).forEach(loc::removeSubLocation);
                subLocations.forEach(loc::addSubLocation);
                new ArrayList<>(loc.getConnections()).forEach(loc::removeConnection);
                locationConnections.forEach(loc::addConnection);
                break;
            }
            case "Item": {
                Item item = (Item) entry;
                item.setItemType(itemTypeField.getText().trim());
                item.setRarity((String) itemRarityCombo.getSelectedItem());
                item.setPower(itemPowerArea.getText().trim());
                // Owner history: append any new owners not already in the list
                List<String> existing = item.getOwnerHistory();
                for (String owner : itemOwners) {
                    if (!existing.contains(owner)) item.addOwner(owner);
                }
                break;
            }
            case "Faction": {
                Faction f = (Faction) entry;
                f.setGoal(factionGoalArea.getText().trim());
                f.setIdeology(factionIdeologyArea.getText().trim());
                new ArrayList<>(f.getMembers()).forEach(f::removeMember);
                factionMembers.forEach(f::addMember);
                new ArrayList<>(f.getFactionRelationships()).forEach(f::removeFactionRelationship);
                factionRelationships.forEach(f::addFactionRelationship);
                break;
            }
            case "Lore": {
                LoreEntry lore = (LoreEntry) entry;
                lore.setEra((String) loreEraCombo.getSelectedItem());
                new ArrayList<>(lore.getTimeline()).forEach(lore::removeTimelineEntry);
                loreTimeline.forEach(lore::addTimelineEntry);
                new ArrayList<>(lore.getConsequences()).forEach(lore::removeConsequence);
                loreConsequences.forEach(lore::addConsequence);
                new ArrayList<>(lore.getReferences()).forEach(lore::removeReference);
                loreReferences.forEach(lore::addReference);
                break;
            }
        }
    }

    private void resetAllLists(){
        charAffiliations.clear(); charAbilities.clear(); charRelationships.clear();
        subLocations.clear(); locationConnections.clear();
        itemOwners.clear();
        factionMembers.clear(); factionRelationships.clear();
        loreTimeline.clear(); loreConsequences.clear(); loreReferences.clear();
    }

    private void prefillListsFrom(CodexEntry entry){
        switch (entry.getType()) {
            case "Character": {
                Model.Character c = (Model.Character) entry;
                charAffiliations.addAll(c.getAffiliations());
                charAbilities.addAll(c.getAbilities());
                charRelationships.addAll(c.getRelationships());
                break;
            }
            case "Location": {
                Location loc = (Location) entry;
                subLocations.addAll(loc.getSubLocations());
                locationConnections.addAll(loc.getConnections());
                break;
            }
            case "Item": {
                itemOwners.addAll(((Item) entry).getOwnerHistory());
                break;
            }
            case "Faction": {
                Faction f = (Faction) entry;
                factionMembers.addAll(f.getMembers());
                factionRelationships.addAll(f.getFactionRelationships());
                break;
            }
            case "Lore": {
                LoreEntry lore = (LoreEntry) entry;
                loreTimeline.addAll(lore.getTimeline());
                loreConsequences.addAll(lore.getConsequences());
                loreReferences.addAll(lore.getReferences());
                break;
            }
        }
    }

    private void handleCancel(){
        if(entryToEdit != null){
            mainFrame.navigateToEntry(currentWorld, entryToEdit);
        } else {
            mainFrame.navigateToWorldView();
        }
    }

    private void showValidationError(String message){
        NotificationManager.showWarning(mainFrame, message);
    }

    private JPanel buildSection(String title) {
        JPanel section = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
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
        section.setBorder(new EmptyBorder(ThemeConstants.padding, ThemeConstants.padding,
                ThemeConstants.padding, ThemeConstants.padding));

        JLabel heading = new JLabel(title);
        heading.setFont(ThemeConstants.fontHeading);
        heading.setForeground(ThemeConstants.colorAccent);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(heading);
        section.add(vertSpacer(8));
        return section;
    }

    private JTextField buildTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(ThemeConstants.fontBody);
        field.setForeground(ThemeConstants.colorTextPrimary);
        field.setBackground(ThemeConstants.colorSurface);
        field.setCaretColor(ThemeConstants.colorTextPrimary);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConstants.colorBorder),
                new EmptyBorder(6, 8, 6, 8)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        field.setText(placeholder);
        field.setForeground(ThemeConstants.colorTextPlaceholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(ThemeConstants.colorTextPrimary);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(ThemeConstants.colorTextPlaceholder);
                }
            }
        });
        return field;
    }

    private JTextArea buildTextArea(String placeholder, int rows) {
        JTextArea area = new JTextArea(rows, 0);
        area.setFont(ThemeConstants.fontBody);
        area.setForeground(ThemeConstants.colorTextPrimary);
        area.setBackground(ThemeConstants.colorSurface);
        area.setCaretColor(ThemeConstants.colorTextPrimary);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(6, 8, 6, 8));
        return area;
    }

    private JScrollPane scrollWrap(JTextArea area, int height) {
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(ThemeConstants.colorBorder));
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return sp;
    }

    private JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeConstants.fontSmall);
        label.setForeground(ThemeConstants.colorTextSecondary);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private Component vertSpacer(int height) {
        JPanel sp = new JPanel();
        sp.setOpaque(false);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        sp.setPreferredSize(new Dimension(0, height));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sp;
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
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); button.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { button.setBackground(bgColor);    button.repaint(); }
        });
        return button;
    }
}