package edu.brown.cs.athenia.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.brown.cs.athenia.data.modules.Tag;

public class Language extends Modularized {
  private String name;
  private Map<String, Tag> tags;
  private List<FreeNote> freenotes;

  // TODO: some sort of recent list storing the most recent
  // free notes according to some date value

  /**
   * Constructors
   */

  public Language(String name) {
    this.name = name;
    this.tags = new HashMap<String, Tag>();
    this.freenotes = new ArrayList<FreeNote>();

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

  public void addTag(String t) {
    tags.put(t, new Tag(t));
  }

  public Tag removeTag(String t) {
    return tags.remove(t);
  }

  public boolean hasTag(String tag) {
    return tags.containsKey(tag);
  }

  public List<FreeNote> getRecentFreeNotes() {
    /*
    List<FreeNote> returnable = new ArrayList<FreeNote>();

    int i = 1;
    while (i <= 3) {
      FreeNote note = freenotes.get(freenotes.size() - i);
      if (note != null) {
        returnable.add(note);
      }
    }

    return returnable;
    */

    // a list of the users recent notes
    List<FreeNote> recentNotes = new ArrayList<>();

    // the max number of notes to return
    int NUMBER_OF_NOTES = 3;

    for (int i = 0; i < freenotes.size() && i < NUMBER_OF_NOTES; i++) {
      FreeNote note = freenotes.get(i);
      recentNotes.add(note);
    }

    return recentNotes;

  }

}
