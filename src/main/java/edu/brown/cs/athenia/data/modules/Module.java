package edu.brown.cs.athenia.data.modules;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.brown.cs.athenia.data.FreeNote;

/**
 * Module is a class that stipulates all modules have a set of tags, add and
 * remove tags methods, and date last review.
 * @author makaylamurphy
 *
 */
public abstract class Module {

  private Map<String, Tag> tags;
  private Date created;
  private Date dateModified;
  private String id;
  private FreeNote freeNote;

  public Module() {
    this.freeNote = null;
    created = new Date();
    dateModified = created;
    this.tags = new HashMap<String, Tag>();
  }

  public Module(FreeNote f) {
    this.freeNote = f;
    created = new Date();
    dateModified = created;
    this.tags = new HashMap<String, Tag>();
  }

  public FreeNote getFreeNote() {
    return this.freeNote;
  }

  public boolean hasFreeNote() {
    return this.freeNote != null;
  }

  public void removeFreeNote() {
    this.freeNote = null;
  }

  // TODO: an id field and getters / setters for it? DONE
  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
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

  /**
   * Setter for date modified.
   * @param m
   *          The new date modified.
   */
  public void setDateModified(Date m) {
    // TODO : indicate this change in the database
    dateModified = m;
  }

  /**
   * Getter for date created.
   * @return The date it was created.
   */
  public Date getDateCreated() {
    return created;
  }

  /**
   * Getter for the date modified last.
   * @return The date modified last.
   */
  public Date getDateModified() {
    return dateModified;
  }

}
