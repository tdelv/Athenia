package edu.brown.cs.athenia.data.modules;

import java.util.Date;
import java.util.HashSet;

/**
 * Module is a class that stipulates all modules have a set of tags, add and
 * remove tags methods, and date last review.
 * @author makaylamurphy
 *
 */
public class Module {

  // rating is originally set to 1 (medium).
  private int rating;
  private HashSet<String> tags;
  private Date created;
  private Date dateModified;

  public Module() {
    rating = 1;
    created = new Date();
    dateModified = created;
    tags = new HashSet<String>();
  }

  /**
   * Getter for the rating from 0 (easy) to 2 (hard).
   * @return Returns int rating.
   */
  public int getRating() {
    return this.rating;
  }

  public void setRating(int r) {
    this.rating = r;
  }

  /**
   * Getter for list of tags.
   * @return Returns the HashSet of String tags.
   */
  public HashSet<String> getTags() {
    return this.tags;
  }

  /**
   * Adds a tag to the HashSet of tags.
   * @param tag
   *          String tag
   */
  public void addTag(String tag) {
    this.tags.add(tag);
  }

  /**
   * Removes the passed in String tag.
   * @param tag
   * @return Returns the boolean if the String tag is found.
   */
  public boolean removeTag(String tag) {
    return this.tags.remove(tag);
  }

  /**
   * Setter for date modified.
   * @param m
   *          The new date modified.
   */
  public void setDateModified(Date m) {
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
