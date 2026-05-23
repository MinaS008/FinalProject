package Model;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public abstract class CodexEntry implements Serializable{
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String id;
    private String name;
    private String description;
    private final String createdAt;
    private String updatedAt;

    public CodexEntry(String id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;

        String now = LocalDateTime.now().format(FORMATTER);
        this.createdAt = now;
        this.updatedAt = now;
    }

    public abstract String getType();
    public abstract String getSummary();
    public abstract CodexEntry deepCopy();
    //Getters
    public String getID(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String getCreatedAt(){
        return createdAt;
    }

    public String getUpdatedAt(){
        return updatedAt;
    }

    //Setters
    public void setName(String name){
        this.name = name;
        refreshUpdatedAt();
    }

    public void setDescription(String description){
        this.description = description;
        refreshUpdatedAt();
    }

    //Helpers
    public void refreshUpdatedAt(){
        this.updatedAt = LocalDateTime.now().format(FORMATTER);
    }

    public String toString(){
        return String.format("[%s] %s (ID: %s) - %s", getType(), name, id, description);
    }

    public boolean equals(Object object){
        if(this == object){
            return true;
        }

        if(!(object instanceof CodexEntry other)) return false;
        return this.id.equals(other.id);
    }

    public int hashCode(){
        return id.hashCode();
    }

}
