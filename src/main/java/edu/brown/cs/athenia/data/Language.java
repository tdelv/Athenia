package edu.brown.cs.athenia.data;

import edu.brown.cs.athenia.data.modules.FreeNote;
import edu.brown.cs.athenia.data.modules.module.Conjugation;
import edu.brown.cs.athenia.data.modules.module.Note;
import edu.brown.cs.athenia.data.modules.module.Tag;
import edu.brown.cs.athenia.data.modules.module.Vocab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Language {
    private List<FreeNote> freeNoteList;
    private List<Conjugation> conjugationList;
    private List<Note> noteList;
    private List<Tag> tagList;
    private List<Vocab> vocabList;

    // TODO: some sort of recent list storing the most recent
    //              free notes according to some date value

    /**
     * Constructors
     */

    public Language() {
        this.freeNoteList = new ArrayList<>();
        this.conjugationList = new ArrayList<>();
        this.noteList = new ArrayList<>();
        this.tagList = new ArrayList<>();
        this.vocabList = new ArrayList<>();
    }

    public Language(
            List<FreeNote> freeNoteList,
            List<Conjugation> conjugationList,
            List<Note> noteList,
            List<Tag> tagList,
            List<Vocab> vocabList
    ) {
        this.freeNoteList = new ArrayList<FreeNote>(freeNoteList);
        this.conjugationList = new ArrayList<Conjugation>(conjugationList);
        this.noteList = new ArrayList<Note>(noteList);
        this.tagList = new ArrayList<Tag>(tagList);
        this.vocabList = new ArrayList<Vocab>(vocabList);
    }

    /**
     * Getters
     */

    public List<FreeNote> getFreeNoteList() {
        return new ArrayList<FreeNote>(freeNoteList);
    }

    public List<Conjugation> getConjugationList() {
        return new ArrayList<Conjugation>(conjugationList);
    }

    public List<Note> getNoteList() {
        return new ArrayList<Note>(noteList);
    }

    public List<Tag> getTagList() {
        return new ArrayList<Tag>(tagList);
    }

    public List<Vocab> getVocabList() {
        return new ArrayList<Vocab>(vocabList);
    }

    public int getVocabCount() {
        return vocabList.size();
    }

    public int getNoteCount() {
        return freeNoteList.size();
    }

    public int getConjugationCount() {
        return conjugationList.size();
    }

    // TODO : a getter for retrieving the most recent FreeNotes
    public List<FreeNote> getRecentFreeNotes() {
        return null;
    }

    /**
     * Adders
     */

    public void addFreeNote(FreeNote freeNote) {
        this.freeNoteList.add(freeNote);
    }

    public void addConjugation(Conjugation conjugation) {
        this.conjugationList.add(conjugation);
    }

    public void addNote(Note note) {
        this.noteList.add(note);
    }

    public void addTag(Tag tag) {
        this.tagList.add(tag);
    }

    public void addVocab(Vocab vocab) {
        // TODO update the vocab table in database
        this.vocabList.add(vocab);
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
    }

    public void deleteTag(String id) {
        // TODO : delete the Tag from this object, the database, and
        //              and all modules that have this tag?
    }


}
