package Controller;

import Model.LoreEntry;
import Model.TimelineEvent;
import Model.World;
import Utilities.Validator;
import Utilities.Validator.ValidationResult;
import Utilities.IDGenerator;

import java.util.*;

public class TimelineManager {
    public static final String[] ERAS = {
            LoreEntry.eraMythic,
            LoreEntry.eraAncient,
            LoreEntry.eraClassical,
            LoreEntry.eraMedieval,
            LoreEntry.eraModern,
            LoreEntry.eraFuture,
            LoreEntry.eraUnknown
    };

    private final IDGenerator idGenerator;

    public TimelineManager(){
        this.idGenerator = new IDGenerator();
    }

    //Create
    public TimelineEvent createEvent(World world,
                                     String title,
                                     String description,
                                     String era,
                                     String dateLabel,
                                     int sortOrder) {
        Objects.requireNonNull(world, "World cannot be null.");

        ValidationResult nameResult = Validator.validateEntryBase(title, description);
        if(!nameResult.isValid()){
            throw new IllegalArgumentException(nameResult.getMessage());
        }

        if (era == null || era.isBlank()){
            throw new IllegalArgumentException("Era cannot be empty.");
        }

        String id = idGenerator.generatedID();
        TimelineEvent event = new TimelineEvent(id, title, description, era, dateLabel, sortOrder);
        world.addTimelineEvent(event);
        return event;
    }

    public List<TimelineEvent> getEventsSorted(World world){
        Objects.requireNonNull(world, "World cannot be null.");
        List<TimelineEvent> events = world.getTimelineEvents();
        events.sort(Comparator.comparingInt(TimelineEvent::getSortOrder)
                .thenComparing(TimelineEvent::getTitle));
        return events;
    }

    public List<TimelineEvent> getEventsByEra(World world, String era){
        List<TimelineEvent> sorted = getEventsSorted(world);
        List<TimelineEvent> result = new ArrayList<>();
        for (TimelineEvent e : sorted) {
            if (e.getEra().equalsIgnoreCase(era)) result.add(e);
        }
        return result;
    }

    public int nextSortOrder(World world){
        Objects.requireNonNull(world, "World cannot be null.");
        return world.getTimelineEvents().stream()
                .mapToInt(TimelineEvent::getSortOrder)
                .max()
                .orElse(0) + 10;  // step by 10 to leave room for insertions
    }

    public void updateEvent(World world,
                            String eventId,
                            String newTitle,
                            String newDescription,
                            String newEra,
                            String newDateLabel,
                            int newSortOrder){
        Objects.requireNonNull(world, "World cannot be null.");
        TimelineEvent event = requireEvent(world, eventId);

        ValidationResult nameResult = Validator.validateEntryBase(newTitle, newDescription);
        if (!nameResult.isValid()) {
            throw new IllegalArgumentException(nameResult.getMessage());
        }
        if (newEra == null || newEra.isBlank()) {
            throw new IllegalArgumentException("Era cannot be empty.");
        }

        event.setTitle(newTitle);
        event.setDescription(newDescription);
        event.setEra(newEra);
        event.setDateLabel(newDateLabel);
        event.setSortOrder(newSortOrder);
        world.refreshUpdatedAt();
    }

    public boolean deleteEvent(World world, String eventID){
        Objects.requireNonNull(world, "World cannot be null");
        return world.removeTimelineEvent(eventID);
    }

    //Involved entities/Consequence helpers
    public void addInvolvedEntity(World world, String eventID, String entityName){
        ValidationResult r = Validator.validateListItem(entityName, "Entity name");
        if(!r.isValid()) throw new IllegalArgumentException(r.getMessage());
        requireEvent(world, eventID).addInvolvedEntity(entityName);
    }

    public void addConsequence(World world, String eventID, String consequence){
        ValidationResult r = Validator.validateListItem(consequence, "Consequence");
        if (!r.isValid()) throw new IllegalArgumentException(r.getMessage());
        requireEvent(world, eventID).addConsequence(consequence);
    }

    //Statistics helper
    public Map<String, Integer> getEraDistribution(World world){
        Map<String, Integer> distribution = new LinkedHashMap<>();
        for(String era: ERAS) distribution.put(era, 0);
        for(TimelineEvent e: world.getTimelineEvents()){
            distribution.merge(e.getEra(), 1, Integer::sum);
        }

        distribution.entrySet().removeIf(entry->entry.getValue() == 0);
        return distribution;
    }

    private TimelineEvent requireEvent(World world, String eventID){
        TimelineEvent event = world.getTimelineEventById(eventID);
        if (event == null) {
            throw new NoSuchElementException(
                    "No TimelineEvent with ID '" + eventID + "' found in world '"
                            + world.getName() + "'.");
        }
        return event;
    }
}