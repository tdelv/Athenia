package edu.brown.cs.athenia.data.modules;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

  /**
   * If this Module is made without being in a FreeNote.
   */
  public Module() {
    this.freeNote = null;
    this.id = new BigInteger(130, new SecureRandom()).toString(32);
    this.created = new Date();
    this.dateModified = new Date();
    this.dateLastReviewed = new Date();
    this.tags = new HashMap<String, Tag>();
  }

  /**
   * if a Module is made with being in a FreeNote.
   * @param f
   */
  public Module(FreeNote f) {
    this.freeNote = f;
    this.id = new BigInteger(130, new SecureRandom()).toString(32);
    this.created = new Date();
    this.dateModified = new Date();
    this.dateLastReviewed = new Date();
    this.tags = new HashMap<String, Tag>();
  }

  /**
   * Sets this Module's FreeNote.
   * @param note
   */
  public void setFreeNote(FreeNote note) {
    this.freeNote = note;
  }

  /**
   * Getter for FreeNote.
   * @return
   */
  public FreeNote getFreeNote() {
    return this.freeNote;
  }

  /**
   * Check if a Module is in a FreeNote.
   * @return
   */
  public boolean hasFreeNote() {
    return this.freeNote != null;
  }

  /**
   * Remove a Module's FreeNote.
   */
  public void removeFreeNote() {
    this.freeNote = null;
  }

  /**
   * Replace all of the Tags wihtin a Module with the given Tags.
   * @param tags
   */
  public void replaceAllTags(List<Tag> tags) {
    this.tags.clear();
    for (Tag t : tags) {
      this.tags.put(t.getTag(), t);
    }
  }

  // TODO: an id field and getters / setters for it? DONE
  /**
   * Setter for id.
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gettter for id.
   * @return
   */
  public String getId() {
    return this.id;
  }

  /***
   * Getter for tags.
   * @return
   */
  public Collection<Tag> getTags() {
    return new HashSet<>(tags.values());
  }

  /**
   * Adds a Tag to a Module.
   * @param t
   */
  public void addTag(Tag t) {
    tags.put(t.getTag(), t);
  }

  /**
   * Remover for a tag.
   * @param t
   * @return
   */
  public Tag removeTag(String t) {
    return tags.remove(t);
  }

  /**
   * Check if has a tag.
   * @param tag
   * @return
   */
  public boolean hasTag(String tag) {
    return tags.containsKey(tag);
  }

  /**
   * Getter for a tag by String tag meaning.
   * @param tag
   * @return
   */
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

  /**
   * Abstract getter for the StorageType of an object.
   * @return
   */
  public abstract StorageType getType();

}
