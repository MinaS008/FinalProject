package UI;
import Helper.*;
import Controller.*;
import Persistence.*;
import Model.*;


import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class MainFrame extends JFrame {
    public static final String panelDashboard = "DASHBOARD";

    public static final String panelWorldView = "WORLD VIEW";
    public static final String panelEntryEditor = "ENTRY EDITOR";
    public static final String panelEntryDetail = "ENTRY DETAIL";

    private WorldManager manager;
    private DataStore dataStore;
    private JPanel cardContainer;
    private CardLayout cardLayout;
    private NavigationBar navBar;

    private DashboardPanel dashboardPanel;
    private WorldViewPanel worldViewPanel;
    private EntryEditorPanel entryEditorPanel;
    private EntryDetailPanel entryDetailPanel;

    public MainFrame(){
        super("The Nexus Codex");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);
        applyTheme();
    }

    public void init(){
        manager = new WorldManager();

        try{
            dataStore = new DataStore();
            loadSavedWorlds();
        } catch (IOException e){
            showErrorDialog("Could not initialise save system:\n" + e.getMessage()
                    + "\n\nThe app will run but data will not be saved.");
        }

        navBar = new NavigationBar(this);
        add(navBar, BorderLayout.NORTH);

        buildCardContainer();
        add(cardContainer, BorderLayout.CENTER);
    }

    public void display(){
        setVisible(true);
    }

    private void buildCardContainer(){
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        cardContainer.setBackground(ThemeConstants.colorBackground);

        dashboardPanel = new DashboardPanel(this, manager);
        cardContainer.add(dashboardPanel, panelDashboard);

        worldViewPanel = new WorldViewPanel(this, manager);
        cardContainer.add(worldViewPanel, panelWorldView);

        entryEditorPanel = new EntryEditorPanel(this, manager);
        cardContainer.add(entryEditorPanel, panelEntryEditor);

        entryDetailPanel = new EntryDetailPanel(this, manager);
        cardContainer.add(entryDetailPanel, panelEntryDetail);

        cardLayout.show(cardContainer, panelDashboard);

    }

    public void switchPanel(String panelName){
        cardLayout.show(cardContainer, panelName);
    }

    public void navigateToDashboard(){
        navBar.highlight(NavigationBar.sectionDashboard, null, null);
        dashboardPanel.refresh();
        cardLayout.show(cardContainer, panelDashboard);
    }

    public void navigateToWorld(String worldId) {
        World world = manager.getWorldByID(worldId);
        if (world == null) {
            showErrorDialog("Could not find world with ID: " + worldId);
            return;
        }
        navBar.highlight(NavigationBar.sectionWorldView, world, null);
        worldViewPanel.loadWorld(world);
        cardLayout.show(cardContainer, panelWorldView);
    }

    public void navigateToWorldView() {
        World world = worldViewPanel.getCurrentWorld();
        navBar.highlight(NavigationBar.sectionWorldView, world, null);
        worldViewPanel.refresh();
        cardLayout.show(cardContainer, panelWorldView);
    }

    public void navigateToCreateEntry(World world) {
        navBar.highlight(NavigationBar.sectionEntryEditor, world, "New Entry");
        entryEditorPanel.loadForCreate(world);
        cardLayout.show(cardContainer, panelEntryEditor);
    }

    public void navigateToEditor(World world, CodexEntry entry) {
        navBar.highlight(NavigationBar.sectionEntryEditor, world,
                "Edit: " + entry.getName());
        entryEditorPanel.loadForEdit(world, entry);
        cardLayout.show(cardContainer, panelEntryEditor);
    }

    public void navigateToEntry(World world, CodexEntry entry){
        navBar.highlight(NavigationBar.sectionEntryDetail, world, entry.getName());
        entryDetailPanel.loadEntry(world, entry);
        cardLayout.show(cardContainer, panelEntryDetail);

    }

    private void loadSavedWorlds(){
        try{
            List<World> savedWorlds = dataStore.loadAllWorlds();
            for(World world : savedWorlds){
                manager.registerLoadedID(world.getID());
                for(CodexEntry entry : world.getEntries()){
                    manager.registerLoadedID(entry.getID());
                }
                manager.loadWorld(world);
            }
        } catch (IOException e){
            showErrorDialog("Could not load saved worlds:\n" + e.getMessage());
        }
    }

    public void saveWorld(World world) {
        if (dataStore == null) return;
        try {
            dataStore.saveWorld(world);
        } catch (IOException e) {
            showErrorDialog("Could not save world \"" + world.getName() + "\":\n"
                    + e.getMessage());
        }
    }

    public void deleteSavedWorld(String worldId) {
        if (dataStore == null) return;
        try {
            dataStore.deleteSavedWorld(worldId);
        } catch (IOException e) {
            showErrorDialog("Could not delete save file for world:\n" + e.getMessage());
        }
    }

    private void applyTheme(){
        getContentPane().setBackground(ThemeConstants.colorBackground);
    }

    public void showErrorDialog(String message){
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}