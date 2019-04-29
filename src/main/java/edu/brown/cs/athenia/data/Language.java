package edu.brown.cs.athenia.data;

import edu.brown.cs.athenia.data.modules.FreeNote;
import edu.brown.cs.athenia.data.modules.module.Conjugation;
import edu.brown.cs.athenia.data.modules.module.Note;
import edu.brown.cs.athenia.data.modules.module.Tag;
import edu.brown.cs.athenia.data.modules.module.Vocab;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class Language {
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

    public Language() {
        this.freeNoteMap = new HashMap<>();
        this.conjugationMap = new HashMap<>();
        this.noteMap = new HashMap<>();
        this.tagMap = new HashMap<>();
        this.vocabMap = new HashMap<>();
    }

    public Language(
            Map<String, FreeNote> freeNoteMap,
            Map<String, Conjugation> conjugationMap,
            Map<String, Note> noteMap,
            Map<String, Tag> tagMap,
            Map<String, Vocab> vocabMap
    ) {
        this.freeNoteMap = new HashMap<String, FreeNote>(freeNoteMap);
        this.conjugationMap = new HashMap<String, Conjugation>(conjugationMap);
        this.noteMap = new HashMap<String, Note>(noteMap);
        this.tagMap = new HashMap<String, Tag>(tagMap);
        this.vocabMap = new HashMap<String, Vocab>(vocabMap);
    }

    /**
     * Getters
     */

    public Map<String, FreeNote> getFreeNoteMap() {
        return Collections.unmodifiableMap(freeNoteMap);
    }

    public Map<String, Conjugation> getConjugationMap() {
        return Collections.unmodifiableMap(conjugationMap);
    }

    public Map<String, Note> getNoteMap() {
        return Collections.unmodifiableMap(noteMap);
    }

    public Map<String, Tag> getTagMap() {
        return Collections.unmodifiableMap(tagMap);
    }

    public Map<String, Vocab> getVocabMap() {
        return Collections.unmodifiableMap(vocabMap);
    }

    public int getVocabCount() {
        return vocabMap.size();
    }

    public int getNoteCount() {
        return freeNoteMap.size();
    }

    public int getConjugationCount() {
        return conjugationMap.size();
    }

    // TODO : a getter for retrieving the most recent FreeNotes
    public List<FreeNote> getRecentFreeNotes() {
        return null;
    }

    /**
     * Adders
     */

    public void addFreeNote(FreeNote freeNote) {
        // TODO get the new id of freenote in database
        // TODO add to the map
    }

    public void addConjugation(Conjugation conjugation) {
        // TODO get the new id of conjugation in database
        // TODO add to the map
    }

    public void addNote(Note note) {
        // TODO get the new id of note in database
        // TODO add to the map
    }

    public void addTag(Tag tag) {
        // TODO get the new id of tag in database
        // TODO add to the map
    }

    public void addVocab(Vocab vocab) {
        // TODO get the new vocab of freenote in database
        // TODO add to the map
    }

    /**
     * Updaters
     * TODO : an update method for each type of module with key of map
     *          linking to the ID of that module in the database
     *          and the value being that object itself
     */

    public void updateVocabulary(String id) {
        // TODO : update the vocabulary module in both object and database?
        //          this or just a method to get the module itself and
        //           call update on it where that method then updates
        //          the database and this
        //          > parameters still to be determined
    }

    public void updateTag(String id) {
        // TODO : update the tag information accordingly (the parameters
        //          are still to be determined)
    }

    /**
     * Deleters
     * TODO : a delete method for each type of module with key of map
     *          being ID of module to delete and key being the object itself
     */

    public void deleteVocabulary(String id) {
        // TODO : delete the vocab object in list/map and in database
        vocabMap.remove(id);
    }

    public void deleteTag(String id) {
        // TODO : delete the Tag from this object, the database, and
        //              and all modules that have this tag?
        tagMap.remove(id);
    }


}
