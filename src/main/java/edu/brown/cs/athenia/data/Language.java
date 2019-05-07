package edu.brown.cs.athenia.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.brown.cs.athenia.data.modules.Tag;

/**
 * Language has a ModuleMap from Modularized and represents a user's one
 * language.
 * @author makaylamurphy
 *
 */
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

  /**
   * Getter for Tags.
   * @return
   */
  public Collection<Tag> getTags() {
    return new HashSet<>(tags.values());
  }

  /**
   * Add a tag.
   * @param t
   */
  public void addTag(Tag t) {
    tags.put(t.getTag(), t);
  }

  /**
   * Remover for Tags.
   * @param t
   * @return
   */
  public Tag removeTag(String t) {
    return tags.remove(t);
  }

  /**
   * Check if has tag.
   * @param tag
   * @return
   */
  public boolean hasTag(String tag) {
    return tags.containsKey(tag);
  }

  /**
   * Getter for Tag.
   * @param tag
   * @return
   */
  public Tag getTag(String tag) {
    return tags.get(tag);
  }

  /**
   * Adds a FreeNote to Language.
   * @param note
   */
  public void addFreeNote(FreeNote note) {
    freeNotesMap.put(note.getId(), note);
  }

  /**
   * Check if Language contains a certain FreeNote.
   * @param id
   * @return
   */
  public boolean containsFreeNote(String id) {
    return freeNotesMap.containsKey(id);
  }

  /**
   * Getter for FreeNotes.
   * @param id
   * @return
   */
  public FreeNote getFreeNote(String id) {
    return freeNotesMap.get(id);
  }

  /**
   * Remover for FreeNotes
   * @param id
   */
  public void removeFreeNote(String id) {
    freeNotesMap.remove(id);
  }

  /**
   * Getter for FreeNotes.
   * @return
   */
  public List<FreeNote> getFreeNotes() {
    return new ArrayList<FreeNote>(this.freeNotesMap.values());
  }

  /**
   * Gets the recent FreeNotes to display.
   * @return
   */
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
