package edu.brown.cs.athenia.data;

import edu.brown.cs.athenia.data.modules.FreeNote;
import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.data.modules.module.*;

import java.util.*;

public class Language {
    private String name;
    private Map<StorageType, Map<String, Module>> moduleMap;
    private Map<String, FreeNote> freeNoteMap;
    private Map<String, Conjugation> conjugationMap;
    private Map<String, Note> noteMap;
    private Map<String, Tag> tagMap;
    private Map<String, Vocab> vocabMap;

    // TODO: some sort of recent list storing the most recent
    //              free notes according to some date value

    /**
     * Constructors
     */

    public Language(String name) {
        this.name = name;

        this.moduleMap = new HashMap<>();
        for (StorageType type : StorageType.values()) {
            this.moduleMap.put(type, new HashMap<>());
        }
    }

    public Language(
            String name,
            Map<StorageType, Map<String, Module>> moduleMap
    ) {
        this.name = name;
        this.moduleMap = moduleMap;
    }

    /**
     * Getters
     */

    public String getName() {
        return this.name;
    }

    public Map<String, Module> getModuleMap(StorageType type) {
        return Collections.unmodifiableMap(moduleMap.get(type));
    }

    public Module getModule(StorageType type, String id) {
        return moduleMap.get(type).get(id);
    }

    public int getModuleCount(StorageType type) {
        return moduleMap.get(type).size();
    }

    // TODO : a getter for retrieving the most recent FreeNotes
    public List<FreeNote> getRecentFreeNotes() {
        return new ArrayList<>();
    }

    /**
     * Adders
     */

    // TODO : how does this work? i have actually no idea how to make an instant of any module yet... (jason)
    public void addModule(StorageType type, Module module) {
        moduleMap.get(type).put(module.getId(), module);
    }

    /**
     * Updaters
     * TODO : an update method for each type of module with key of map
     *          linking to the ID of that module in the database
     *          and the value being that object itself
     */

    public void updateModule(StorageType type, String id) {
        // TODO : update the vocabulary module in both object and database?
        //          this or just a method to get the module itself and
        //           call update on it where that method then updates
        //          the database and this
        //          > parameters still to be determined
    }


    /**
     * Deleters
     * TODO : a delete method for each type of module with key of map
     *          being ID of module to delete and key being the object itself
     */

    public void deleteModule(StorageType type, String id) {
        // TODO : delete the vocab object in list/map and in database
        moduleMap.get(type).remove(id);
    }


}
