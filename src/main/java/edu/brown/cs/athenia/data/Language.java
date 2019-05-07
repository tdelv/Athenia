package edu.brown.cs.athenia.data;

import java.util.*;

import edu.brown.cs.athenia.data.modules.Tag;

public class Language extends Modularized {
  private String name;
  private Map<String, Tag> tags;
  private Map<String, FreeNote> freeNotesMap;

  // TODO: some sort of recent list storing the most recent
  // free notes according to some date value

  /**
   * Constructors
   */

  public Language(String name) {
    this.name = name;
    this.tags = new HashMap<String, Tag>();
    this.freeNotesMap = new HashMap<>();
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
    freeNotesMap.put(note.getId(), note);
  }

  public boolean containsFreeNote(String id) {
    return freeNotesMap.containsKey(id);
  }

  public FreeNote getFreeNote(String id) {
    return freeNotesMap.get(id);
  }

  public void removeFreeNote(String id) {
    freeNotesMap.remove(id);
  }

  public List<FreeNote> getFreeNotes() {
    return new ArrayList<FreeNote>(this.freeNotesMap.values());
  }

  public List<FreeNote> getRecentFreeNotes() {

    // a list of the users recent notes
    List<FreeNote> recentNotes = new ArrayList<>();

    List<FreeNote> freeNotes = new ArrayList<>(freeNotesMap.values());

    // the max number of notes to return
    int NUMBER_OF_NOTES = 3;

    for (int i = 0; i < freeNotes.size() && i < NUMBER_OF_NOTES; i++) {
      FreeNote note = freeNotes.get(i);
      recentNotes.add(note);
    }

    return recentNotes;

  }

}
