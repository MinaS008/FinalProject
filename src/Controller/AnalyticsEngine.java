package Controller;
import Model.*;

import java.util.*;

public class AnalyticsEngine {
    public static class WorldStats {
        public final String worldName;
        public final int totalEntries;

        public final int countCharacters;
        public final int countLocations;
        public final int countItems;
        public final int countFactions;
        public final int countLore;

        public final String mostRecentEntryName;
        public final int createdTodayCount;
        public final int relationshipCount;

        public final Map<String, Integer> itemRarityDistribution;
        public final Map<String, Integer> loreEraDistribution;
        public final Map<String, Integer> characterRoleDistribution;

        public final double avgAbilitiesPerCharacter;
        public final double avgMembersPerFaction;

        public final int complexityScore;

        public static final String[] typeOrder = {"Character", "Location", "Item", "Faction", "Lore"};

        public WorldStats(String worldName,
                          int totalEntries,
                          int countCharacters, int countLocations,
                          int countItems, int countFactions, int countLore,
                          String mostRecentEntryName,
                          int createdTodayCount,
                          int relationshipCount,
                          Map<String, Integer> itemRarityDistribution,
                          Map<String, Integer> loreEraDistribution,
                          Map<String, Integer> characterRoleDistribution,
                          double avgAbilitiesPerCharacter,
                          double avgMembersPerFaction,
                          int complexityScore) {
            this.worldName = worldName;
            this.totalEntries = totalEntries;
            this.countCharacters = countCharacters;
            this.countLocations = countLocations;
            this.countItems = countItems;
            this.countFactions = countFactions;
            this.countLore = countLore;
            this.mostRecentEntryName = mostRecentEntryName;
            this.createdTodayCount = createdTodayCount;
            this.relationshipCount = relationshipCount;
            this.itemRarityDistribution = Collections.unmodifiableMap(itemRarityDistribution);
            this.loreEraDistribution = Collections.unmodifiableMap(loreEraDistribution);
            this.characterRoleDistribution = Collections.unmodifiableMap(characterRoleDistribution);
            this.avgAbilitiesPerCharacter = avgAbilitiesPerCharacter;
            this.avgMembersPerFaction = avgMembersPerFaction;
            this.complexityScore = complexityScore;
        }

        public int countForType(String type) {
            switch (type) {
                case "Character":
                    return countCharacters;
                case "Location":
                    return countLocations;
                case "Item":
                    return countItems;
                case "Faction":
                    return countFactions;
                case "Lore":
                    return countLore;
                default:
                    return 0;
            }
        }
    }

    public static WorldStats compute(World world) {
        List<CodexEntry> entries = world.getEntries();

        int chars = 0, locs = 0, items = 0, factions = 0, lore = 0;

        String mostRecentName = "-";
        String mostRecentTimestamp = "";
        int createdToday = 0;
        String today = todayString();

        Map<String, Integer> rarityDist = new LinkedHashMap<>();
        Map<String, Integer> eraDist = new LinkedHashMap<>();
        Map<String, Integer> roleDist = new LinkedHashMap<>();

        int totalAbilities = 0;
        int totalMembers = 0;

        for (CodexEntry entry : entries) {
            //Most-recently updated
            String updatedAt = entry.getUpdatedAt();
            if (updatedAt.compareTo(mostRecentTimestamp) > 0) {
                mostRecentTimestamp = updatedAt;
                mostRecentName = entry.getName();
            }

            //Created today
            if (entry.getCreatedAt().startsWith(today)) {
                createdToday++;
            }

            //Per-type
            switch (entry.getType()) {
                case "Character": {
                    chars++;
                    Model.Character c = (Model.Character) entry;
                    totalAbilities += c.getAbilities().size();
                    // Role distribution (normalize to title-case bucket)
                    String role = c.getRole().isBlank() ? "Unspecified" : c.getRole();
                    roleDist.merge(role, 1, Integer::sum);
                    break;
                }
                case "Location":
                    locs++;
                    break;
                case "Item": {
                    items++;
                    Item item = (Item) entry;
                    String rarity = item.getRarity().isBlank() ? "Unknown" : item.getRarity();
                    rarityDist.merge(rarity, 1, Integer::sum);
                    break;
                }
                case "Faction": {
                    factions++;
                    Faction f = (Faction) entry;
                    totalMembers += f.getMembers().size();
                    break;
                }
                case "Lore": {
                    lore++;
                    LoreEntry le = (LoreEntry) entry;
                    String era = le.getEra().isBlank() ? "Unknown" : le.getEra();
                    eraDist.merge(era, 1, Integer::sum);
                    break;
                }
            }
        }

        double avgAbilities = chars > 0 ? (double) totalAbilities / chars : 0.0;
        double avgMembers = factions > 0 ? (double) totalMembers / factions : 0.0;

        int relCount = countRelationships(world);
        int score = computeComplexityScore(
                chars, locs, items, factions, lore, relCount,
                totalAbilities, totalMembers
        );

        return new WorldStats(
                world.getName(),
                entries.size(),
                chars, locs, items, factions, lore,
                mostRecentName,
                createdToday,
                relCount,
                rarityDist,
                eraDist,
                roleDist,
                avgAbilities,
                avgMembers,
                score
        );
    }

    private static int countRelationships(World world){
        RelationshipGraph graph = world.getRelationshipGraph();
        if(graph == null) return 0;

        Set<String> seen = new HashSet<>();
        int count = 0;
        for(CodexEntry entry: world.getEntries()){
            List<RelationshipGraph.RelationshipLink> links =
                    graph.getConnections(entry.getID());
            for (RelationshipGraph.RelationshipLink link : links){
                String key = minOf(link.getFromID(), link.getToID())
                        + "|" + maxOf(link.getFromID(), link.getToID())
                        + "|" + link.getLabel();
                if(seen.add(key)) count++;
            }
        }
        return count;
    }

    //Complexity score
    private static int computeComplexityScore(int chars, int locs, int items, int factions, int lore, int relationships, int totalAbilities, int totalMembers){
        int score = 0;

        if (chars > 0) score += 10;
        if (locs > 0) score += 10;
        if (items > 0) score += 10;
        if (factions > 0) score += 10;
        if (lore > 0) score += 10;

        // Entry volume
        score += (chars + locs + items + factions + lore) * 5;

        // Relationship depth
        score += relationships * 8;

        // Detail depth
        score += totalAbilities * 2;
        score += totalMembers   * 2;

        return score;
    }

    //Utility
    private static String todayString(){
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private static String minOf(String a, String b){ return a.compareTo(b) <= 0 ? a : b;}
    private static String maxOf(String a, String b){return a.compareTo(b) >= 0 ? a : b;}

    private AnalyticsEngine(){}
}
