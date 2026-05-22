package Controller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import Model.*;

public class SearchEngine {
    public static final String sortNameAsc = "Name (A -> Z)";
    private static final String sortNameDesc = "Name (Z -> A)";
    private static final String sortDateNew = "Newest First";
    private static final String sortDateOld = "Oldest First";

    public static final String[] sortOptions = {
            sortNameAsc, sortNameDesc, sortDateNew, sortDateOld
    };

    private SearchEngine() {}

    public static List<CodexEntry> searchByName(List<CodexEntry> entries, String query){
        if(query == null || query.isBlank()) return new ArrayList<>(entries);

        String lowerQuery = query.toLowerCase().trim();
        List<CodexEntry> results = new ArrayList<>();

        for(CodexEntry entry : entries){
            if(entry.getName().toLowerCase().contains(lowerQuery)){
                results.add(entry);
            }
        }
        return results;
    }

    public static List<CodexEntry> searchByKeyword(List<CodexEntry> entries, String query){
        if(query == null || query.isBlank()) return new ArrayList<>(entries);

        String lowerQuery = query.toLowerCase().trim();
        List<CodexEntry> results = new ArrayList<>();

        for(CodexEntry entry : entries){
            boolean nameMatch = entry.getName().toLowerCase().contains(lowerQuery);
            boolean descriptionMatch = entry.getDescription().toLowerCase().contains(lowerQuery);
            boolean summaryMatch = entry.getSummary().toLowerCase().contains(lowerQuery);

            if(nameMatch || descriptionMatch || summaryMatch){
                results.add(entry);
            }
        }

        return results;
    }

    //Filter
    public static List<CodexEntry> filterByType(List<CodexEntry> entries, String type){
        if(type == null || type.isBlank()) return new ArrayList<>(entries);

        List<CodexEntry> results = new ArrayList<>();
        for(CodexEntry entry : entries){
            if(entry.getType().equalsIgnoreCase(type)){
                results.add(entry);
            }
        }

        return results;
    }

    public static List<CodexEntry> sortResults(List<CodexEntry> entries, String sortMode){
        List<CodexEntry> sorted = new ArrayList<>(entries);

        switch(sortMode){
            case sortNameAsc:
                Collections.sort(sorted, Comparator.comparing(e -> e.getName().toLowerCase()));
                break;

            case sortNameDesc:
                Collections.sort(sorted, (a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                break;

            case sortDateNew:
                Collections.sort(sorted, (a, b)-> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
                break;

            case sortDateOld:
                Collections.sort(sorted, Comparator.comparing(CodexEntry::getUpdatedAt));
                break;

            default:
                break;
        }
        return sorted;
    }

    public static List<CodexEntry> search(List<CodexEntry> entries,
                                          String query,
                                          String type,
                                          String sortMode) {
        List<CodexEntry> results = new ArrayList<>(entries);

        if(query != null && !query.isBlank()){
            results = searchByKeyword(results, query);
        }

        if(type != null && !type.isBlank()){
            results = filterByType(results, type);
        }

        results = sortResults(results, sortMode);
        return results;
    }
}