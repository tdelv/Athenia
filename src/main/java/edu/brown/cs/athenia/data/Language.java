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
        this.vocabList.add(vocab);
    }
}
