package edu.brown.cs.athenia.data;

import java.util.*;

import edu.brown.cs.athenia.data.modules.Tag;

public class Language extends Modularized {
  private String name;
  private Map<String, Tag> tags;
  private Map<String, FreeNote> freenotesMap;
  private List<FreeNote> freeNotes;

  // TODO: some sort of recent list storing the most recent
  // free notes according to some date value

  /**
   * Constructors
   */

  public Language(String name) {
    this.name = name;
    this.tags = new HashMap<String, Tag>();
    this.freenotesMap = new HashMap<>();
    this.freeNotes = new ArrayList<FreeNote>();
  }

  /**
   * Getters
   */

  public String getName() {
    return this.name;
  }

  public Collection<Tag> getTags() {
    return tags.values();
  }

  public void addTag(Tag t) {
    tags.put(t.getTag(), t);
  }

  public Tag removeTag(String t) {
    return tags.remove(t);
  }

  public boolean hasTag(String tag) {
    return tags.containsKey(tag);
  }

  public Tag getTag(String tag) {
    return tags.get(tag);
  }

  public void addFreeNote(FreeNote note) {
    freenotesMap.put(note.getId(), note);
    freeNotes.add(note);
  }

  public boolean containsFreeNote(String id) {
    return freenotesMap.containsKey(id);
  }

  public FreeNote getFreeNote(String id) {
    return freenotesMap.get(id);
  }

  public List<FreeNote> getFreeNotes() {
    return new ArrayList<FreeNote>(this.freeNotes);
  }

  public List<FreeNote> getRecentFreeNotes() {
    /*
     * List<FreeNote> returnable = new ArrayList<FreeNote>();
     * 
     * int i = 1; while (i <= 3) { FreeNote note =
     * freenotes.get(freenotes.size() - i); if (note != null) {
     * returnable.add(note); } }
     * 
     * return returnable;
     */

    // a list of the users recent notes
    List<FreeNote> recentNotes = new ArrayList<>();

    // the max number of notes to return
    int NUMBER_OF_NOTES = 3;

    for (int i = 0; i < freeNotes.size() && i < NUMBER_OF_NOTES; i++) {
      FreeNote note = freeNotes.get(i);
      recentNotes.add(note);
    }

    return recentNotes;

  }

}
