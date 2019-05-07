package edu.brown.cs.athenia.data.modules;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

import edu.brown.cs.athenia.data.FreeNote;
import edu.brown.cs.athenia.data.modules.module.StorageType;

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
  private Date dateLastReviewed;
  private StorageType type;

  public Module() {
    this.freeNote = null;
    this.id = new BigInteger(130, new SecureRandom()).toString(32);
    this.created = new Date();
    this.dateModified = new Date();
    this.dateLastReviewed = new Date();
    this.tags = new HashMap<String, Tag>();
  }

  public Module(FreeNote f) {
    this.freeNote = f;
    this.id = new BigInteger(130, new SecureRandom()).toString(32);
    this.created = new Date();
    this.dateModified = new Date();
    this.dateLastReviewed = new Date();
    this.tags = new HashMap<String, Tag>();
  }

  public void setFreeNote(FreeNote note) {
    this.freeNote = note;
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

  public void replaceAllTags(List<Tag> tags) {
    this.tags.clear();
    for (Tag t : tags) {
      this.tags.put(t.getTag(), t);
    }
  }

  // TODO: an id field and getters / setters for it? DONE
  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }

  public Collection<Tag> getTags() {
    return new HashSet<>(tags.values());
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

  /**
   * Setter for date last reviewed.
   * @param m
   *          The new date last review.
   */
  public void setDateLastReviewed(Date m) {
    // TODO : indicate this change in the database
    this.dateLastReviewed = m;
  }

  /**
   * Getter for date last reviewed.
   * @return The date it was last reviewed.
   */
  public Date getDateLastReviewed() {
    return this.dateLastReviewed;
  }

  public abstract StorageType getType();

}
