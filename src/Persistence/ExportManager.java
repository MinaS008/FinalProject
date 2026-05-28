package Persistence;

import Model.*;
import Model.RelationshipGraph.RelationshipLink;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportManager {
    public static void exportWorld(Component parent, World world){
        String format = pickFormat(parent, "Export World - Choose Format");
        if(format == null) return;

        JFileChooser chooser = buildChooser(world.getName(), format);
        if(chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = enforceExtension(chooser.getSelectedFile(), format);

        try{
            String content = format.equals("json") ? worldToJSON(world) : worldToText(world);
            writeFile(file, content);
            showSuccess(parent, file);
        } catch (IOException e){
            showError(parent, e);
        }
    }

    public static void exportEntry(Component parent, World world, CodexEntry entry){
        String format = pickFormat(parent, "Export Entry - Choose Format");
        if(format == null) return;

        JFileChooser chooser = buildChooser(entry.getName(), format);
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = enforceExtension(chooser.getSelectedFile(), format);

        try {
            String content = format.equals("json")
                    ? entryToJson(entry, world)
                    : entryToText(entry, world);
            writeFile(file, content);
            showSuccess(parent, file);
        } catch (IOException e) {
            showError(parent, e);
        }
    }

    private static String worldToJSON(World world){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"exportedAt\": ").append(jsonStr(timestamp())).append(",\n");
        sb.append("  \"world\": {\n");
        sb.append("    \"id\": ").append(jsonStr(world.getID())).append(",\n");
        sb.append("    \"name\": ").append(jsonStr(world.getName())).append(",\n");
        sb.append("    \"description\": ").append(jsonStr(world.getDescription())).append(",\n");
        sb.append("    \"createdAt\": ").append(jsonStr(world.getCreatedAt())).append(",\n");
        sb.append("    \"updatedAt\": ").append(jsonStr(world.getUpdatedAt())).append(",\n");
        sb.append("    \"entryCount\": ").append(world.getEntryCount()).append(",\n");

        // Entries array
        sb.append("    \"entries\": [\n");
        List<CodexEntry> entries = world.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            sb.append(entryToJsonBlock(entries.get(i), "      "));
            if (i < entries.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    ],\n");

        // Relationships array
        sb.append("    \"relationships\": [\n");
        List<RelationshipLink> links = world.getRelationshipGraph().getAllLinks();
        for (int i = 0; i < links.size(); i++) {
            RelationshipLink l = links.get(i);
            sb.append(" {\n");
            sb.append(" \"from\": ").append(jsonStr(l.getFromID())).append(",\n");
            sb.append(" \"to\": ").append(jsonStr(l.getToID())).append(",\n");
            sb.append(" \"label\": ").append(jsonStr(l.getLabel())).append(",\n");
            sb.append(" \"type\": ").append(jsonStr(l.getType())).append("\n");
            sb.append(" }");
            if (i < links.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    ]\n");

        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String entryToJson(CodexEntry entry, World world){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"exportedAt\": ").append(jsonStr(timestamp())).append(",\n");
        sb.append("  \"world\": ").append(jsonStr(world.getName())).append(",\n");
        sb.append("  \"entry\": \n");
        sb.append(entryToJsonBlock(entry, "  "));
        sb.append("\n}\n");
        return sb.toString();
    }

    private static String entryToJsonBlock(CodexEntry entry, String indent){
        StringBuilder sb = new StringBuilder();
        String i2 = indent + "  ";

        sb.append(indent).append("{\n");
        sb.append(i2).append("\"id\": ").append(jsonStr(entry.getID())).append(",\n");
        sb.append(i2).append("\"type\": ").append(jsonStr(entry.getType())).append(",\n");
        sb.append(i2).append("\"name\": ").append(jsonStr(entry.getName())).append(",\n");
        sb.append(i2).append("\"description\": ").append(jsonStr(entry.getDescription())).append(",\n");
        sb.append(i2).append("\"createdAt\": ").append(jsonStr(entry.getCreatedAt())).append(",\n");
        sb.append(i2).append("\"updatedAt\": ").append(jsonStr(entry.getUpdatedAt())).append(",\n");

        // Type-specific fields
        if (entry instanceof Model.Character c) {
            sb.append(i2).append("\"role\": ").append(jsonStr(c.getRole())).append(",\n");
            sb.append(i2).append("\"backstory\": ").append(jsonStr(c.getBackstory())).append(",\n");
            sb.append(i2).append("\"affiliations\": ").append(jsonList(c.getAffiliations(), i2)).append(",\n");
            sb.append(i2).append("\"abilities\": ").append(jsonList(c.getAbilities(), i2)).append(",\n");
            sb.append(i2).append("\"relationships\": ").append(jsonList(c.getRelationships(), i2)).append("\n");

        } else if (entry instanceof Location loc) {
            sb.append(i2).append("\"locationType\": ").append(jsonStr(loc.getLocationType())).append(",\n");
            sb.append(i2).append("\"region\": ").append(jsonStr(loc.getRegion())).append(",\n");
            sb.append(i2).append("\"subLocations\": ").append(jsonList(loc.getSubLocations(), i2)).append(",\n");
            sb.append(i2).append("\"connections\": ").append(jsonList(loc.getConnections(), i2)).append("\n");

        } else if (entry instanceof Item item) {
            sb.append(i2).append("\"itemType\": ").append(jsonStr(item.getItemType())).append(",\n");
            sb.append(i2).append("\"rarity\": ").append(jsonStr(item.getRarity())).append(",\n");
            sb.append(i2).append("\"power\": ").append(jsonStr(item.getPower())).append(",\n");
            sb.append(i2).append("\"ownerHistory\": ").append(jsonList(item.getOwnerHistory(), i2)).append("\n");

        } else if (entry instanceof Faction f) {
            sb.append(i2).append("\"goal\": ").append(jsonStr(f.getGoal())).append(",\n");
            sb.append(i2).append("\"ideology\": ").append(jsonStr(f.getIdeology())).append(",\n");
            sb.append(i2).append("\"members\": ").append(jsonList(f.getMembers(), i2)).append(",\n");
            sb.append(i2).append("\"factionRelationships\": ")
                    .append(jsonList(f.getFactionRelationships(), i2)).append("\n");

        } else if (entry instanceof LoreEntry lore) {
            sb.append(i2).append("\"era\": ").append(jsonStr(lore.getEra())).append(",\n");
            sb.append(i2).append("\"timeline\": ").append(jsonList(lore.getTimeline(), i2)).append(",\n");
            sb.append(i2).append("\"consequences\": ").append(jsonList(lore.getConsequences(), i2)).append(",\n");
            sb.append(i2).append("\"references\": ").append(jsonList(lore.getReferences(), i2)).append("\n");
        }
        sb.append(indent).append("}");
        return sb.toString();
    }

    private static String worldToText(World world){
        StringBuilder sb = new StringBuilder();
        String divider = "═".repeat(60);
        String subDivider = "-".repeat(60);

        sb.append(divider).append("\n");
        sb.append("THE NEXUS CODEX - WORLD EXPORT\n");
        sb.append(divider).append("\n\n");

        sb.append("World:       ").append(world.getName()).append("\n");
        sb.append("Description: ").append(blankIfEmpty(world.getDescription())).append("\n");
        sb.append("Created:     ").append(world.getCreatedAt()).append("\n");
        sb.append("Updated:     ").append(world.getUpdatedAt()).append("\n");
        sb.append("Entries:     ").append(world.getEntryCount()).append("\n");
        sb.append("Exported:    ").append(timestamp()).append("\n\n");

        // Group entries by type
        String[] types = {"Character", "Location", "Item", "Faction", "Lore"};
        for (String type : types) {
            List<CodexEntry> group = world.getEntriesByType(type);
            if (group.isEmpty()) continue;

            sb.append(divider).append("\n");
            sb.append("  ").append(typeSectionHeader(type)).append(" (").append(group.size()).append(")\n");
            sb.append(divider).append("\n\n");

            for (CodexEntry entry : group) {
                sb.append(entryToTextBlock(entry));
                sb.append(subDivider).append("\n\n");
            }
        }

        // Relationships
        List<RelationshipLink> links = world.getRelationshipGraph().getAllLinks();
        if (!links.isEmpty()) {
            sb.append(divider).append("\n");
            sb.append("  RELATIONSHIPS (").append(links.size()).append(")\n");
            sb.append(divider).append("\n\n");
            for (RelationshipLink link : links) {
                sb.append("  • ").append(link.getFromID())
                        .append("  ──[").append(link.getLabel()).append("]──▶  ")
                        .append(link.getToID()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String entryToText(CodexEntry entry, World world){
        StringBuilder sb = new StringBuilder();
        String divider = "═".repeat(60);

        sb.append(divider).append("\n");
        sb.append("  THE NEXUS CODEX - ENTRY EXPORT\n");
        sb.append(divider).append("\n\n");
        sb.append("World:    ").append(world.getName()).append("\n");
        sb.append("Exported: ").append(timestamp()).append("\n\n");
        sb.append(divider).append("\n\n");
        sb.append(entryToTextBlock(entry));
        return sb.toString();
    }

    private static String entryToTextBlock(CodexEntry entry){
        StringBuilder sb = new StringBuilder();

        sb.append("  [").append(entry.getType().toUpperCase()).append("]  ")
                .append(entry.getName()).append("\n\n");

        sb.append("  Description:\n");
        sb.append(wrapText(entry.getDescription(), "    ", 70)).append("\n\n");

        if (entry instanceof Model.Character c) {
            sb.append("  Role:          ").append(blankIfEmpty(c.getRole())).append("\n");
            sb.append("  Affiliations:  ").append(listOrNone(c.getAffiliations())).append("\n");
            sb.append("  Abilities:     ").append(listOrNone(c.getAbilities())).append("\n");
            sb.append("  Relationships: ").append(listOrNone(c.getRelationships())).append("\n");
            if (!c.getBackstory().isBlank()) {
                sb.append("\n  Backstory:\n");
                sb.append(wrapText(c.getBackstory(), "    ", 70)).append("\n");
            }

        } else if (entry instanceof Location loc) {
            sb.append("  Type:          ").append(blankIfEmpty(loc.getLocationType())).append("\n");
            sb.append("  Region:        ").append(blankIfEmpty(loc.getRegion())).append("\n");
            sb.append("  Sub-locations: ").append(listOrNone(loc.getSubLocations())).append("\n");
            sb.append("  Connections:   ").append(listOrNone(loc.getConnections())).append("\n");

        } else if (entry instanceof Item item) {
            sb.append("  Item Type:     ").append(blankIfEmpty(item.getItemType())).append("\n");
            sb.append("  Rarity:        ").append(blankIfEmpty(item.getRarity())).append("\n");
            sb.append("  Power:         ").append(blankIfEmpty(item.getPower())).append("\n");
            sb.append("  Owner History: ").append(listOrNone(item.getOwnerHistory())).append("\n");

        } else if (entry instanceof Faction f) {
            sb.append("  Goal:          ").append(blankIfEmpty(f.getGoal())).append("\n");
            sb.append("  Ideology:      ").append(blankIfEmpty(f.getIdeology())).append("\n");
            sb.append("  Members:       ").append(listOrNone(f.getMembers())).append("\n");
            sb.append("  Relations:     ").append(listOrNone(f.getFactionRelationships())).append("\n");

        } else if (entry instanceof LoreEntry lore) {
            sb.append("  Era:           ").append(blankIfEmpty(lore.getEra())).append("\n");
            if (!lore.getTimeline().isEmpty()) {
                sb.append("\n  Timeline:\n");
                for (String beat : lore.getTimeline())
                    sb.append("    • ").append(beat).append("\n");
            }
            if (!lore.getConsequences().isEmpty()) {
                sb.append("\n  Consequences:\n");
                for (String con : lore.getConsequences())
                    sb.append("    • ").append(con).append("\n");
            }
            if (!lore.getReferences().isEmpty()) {
                sb.append("\n  References:\n");
                for (String ref : lore.getReferences())
                    sb.append("    • ").append(ref).append("\n");
            }
        }

        sb.append("\n  Created: ").append(entry.getCreatedAt())
                .append("   |   Updated: ").append(entry.getUpdatedAt()).append("\n\n");

        return sb.toString();
    }

    //File/dialog helpers
    private static void writeFile(File file, String content) throws IOException{
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
            writer.write(content);
        }
    }

    private static String pickFormat(Component parent, String title) {
        String[] options = {"JSON (.json)", "Plain Text (.txt)"};
        int choice = JOptionPane.showOptionDialog(
                parent, "Choose an export format:", title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice == 0) return "json";
        if (choice == 1) return "txt";
        return null;
    }

    private static JFileChooser buildChooser(String baseName, String format){
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Export");
        String ext = format.equals("json") ? "json" : "txt";
        String desc = format.equals("json") ? "JSON Files (*.json)" : "Text Files (*.txt)";
        chooser.setFileFilter(new FileNameExtensionFilter(desc, ext));
        chooser.setSelectedFile(new File(sanitiseFilename(baseName) + "." + ext));
        return chooser;
    }

    private static File enforceExtension(File file, String format){
        String ext = "." + format;
        if (!file.getName().toLowerCase().endsWith(ext)) {
            return new File(file.getAbsolutePath() + ext);
        }
        return file;
    }

    private static void showSuccess(Component parent, File file){
        JOptionPane.showMessageDialog(parent,
                "Exported successfully to:\n" + file.getAbsolutePath(),
                "Export Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showError(Component parent, IOException e){
        JOptionPane.showMessageDialog(parent,
                "Export failed:\n" + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
    }

    //JSON string utilities
    private static String jsonStr(String value){
        if (value == null) return "\"\"";
        //Escape backslashes first, then quotes, then control characters
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }

    private static String jsonList(List<String> items, String indent){
        if (items == null || items.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < items.size(); i++) {
            sb.append(indent).append("  ").append(jsonStr(items.get(i)));
            if (i < items.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("]");
        return sb.toString();
    }

    //Text formatting utilities
    private static String blankIfEmpty(String s){
        return (s==null || s.isBlank()) ? "-": s;
    }

    private static String listOrNone(List<String> items){
        return (items == null || items.isEmpty()) ? "none" : String.join(", ", items);
    }

    private static String wrapText(String text, String indent, int maxWidth){
        if (text == null || text.isBlank()) return indent + "—";
        StringBuilder result = new StringBuilder();
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder(indent);
        for (String word : words) {
            if (line.length() + word.length() + 1 > maxWidth + indent.length()) {
                result.append(line).append("\n");
                line = new StringBuilder(indent).append(word);
            } else {
                if (line.length() > indent.length()) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > indent.length()) result.append(line);
        return result.toString();
    }

    private static String sanitiseFilename(String name){
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    private static String typeSectionHeader(String type) {
        switch (type) {
            case "Character": return "CHARACTERS";
            case "Location": return "LOCATIONS";
            case "Item": return "ITEMS";
            case "Faction": return "FACTIONS";
            case "Lore": return "LORE ENTRIES";
            default: return type.toUpperCase() + "S";
        }
    }

    private static String timestamp(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}