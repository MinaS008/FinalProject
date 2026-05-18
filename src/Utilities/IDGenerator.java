package Utilities;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class IDGenerator implements Serializable{
    private static final long serialVersionUID = 1L;

    private Set<String> issuedIDs;

    public IDGenerator(){
        this.issuedIDs = new HashSet<>();
    }

    //Core Methods
    public String generatedID(){
        String newID;

        do{
            newID = "NX-" + UUID.randomUUID().toString();
        } while (issuedIDs.contains(newID));

        issuedIDs.add(newID);
        return newID;
    }

    public boolean isUnique(String id){
        return !issuedIDs.contains(id);
    }

    public void registerExistingID(String id){
        issuedIDs.add(id);
    }

    public int getTotalIssued(){
        return issuedIDs.size();
    }
}