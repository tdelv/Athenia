package edu.brown.cs.athenia.data;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.data.modules.Tag;

/**
 * FreeNote represents our FreeNodes, which have modules, tags, and a state of
 * data created, date modified, and title.
 * @author makaylamurphy
 *
 */
public class FreeNote {

  // tags of the FreeNote itself
  private Map<String, Tag> tags;
  private List<Module> modules;
  private Date created;
  private Date dateModified;
  private String title;
  private String id;

  /**
   * FreeNote constructor to take in String title.
   * @param t
   *          : String title
   */
  public FreeNote(String t) {
    this.id = new BigInteger(130, new SecureRandom()).toString(32);
    this.modules = new ArrayList<Module>();
    this.tags = new HashMap<String, Tag>();
    this.created = new Date();
    this.dateModified = new Date();
    this.title = t;
  }

  /**
   * FreeNote to takes in a given title and id.
   * @param t
   *          : String title
   * @param id
   *          : String id
   */
  public FreeNote(String t, String id) {
    this.id = id;
    this.modules = new ArrayList<Module>();
    this.tags = new HashMap<String, Tag>();
    this.created = new Date();
    this.dateModified = new Date();
    this.title = t;
  }

  /**
   * Setter for title.
   * @param newTitle
   *          : String new title.
   */
  public void setTitle(String newTitle) {
    this.title = newTitle;
  }

  /**
   * Getter for id.
   * @return : String id.
   */
  public String getId() {
    return this.id;
  }

  /**
   * Getter for list of modules.
   * @return : List of Modules
   */
  public List<Module> getModules() {
    return this.modules;
  }

  /**
   * Remover for Modules.
   * @param m
   *          : Module m
   * @return : boolean if Modules are found.
   */
  public boolean removeModule(Module m) {
    this.dateModified = new Date();
    m.removeFreeNote();
    return this.modules.remove(m);
  }

  /**
   * Adds Module to FreeNote.
   * @param m
   */
  public void addModule(Module m) {
    if (m == null) {
      throw new NullPointerException();
    }
    this.dateModified = new Date();
    this.modules.add(m);
  }

  /**
   * Getter for Tags.
   * @return : Collection of Tags.
   */
  public Collection<Tag> getTags() {
    return tags.values();
  }

  /**
   * Getter for Tags.
   * @param t
   *          : Tag t.
   */
  public void addTag(Tag t) {
    this.dateModified = new Date();
    tags.put(t.getTag(), t);
  }

  /**
   * Remover for Tag.
   * @param t
   * @return
   */
  public Tag removeTag(String t) {
    return tags.remove(t);
  }

  public boolean hasTag(String tag) {
    return tags.containsKey(tag);
  }

  /**
   * Setter for date created.
   * @param m
   *          The date created.
   */
  public void setDateCreated(Date m) {
    // TODO : indicate this change in the database
    this.created = m;
  }

  /**
   * Setter for date modified.
   * @param m
   *          The new date modified.
   */
  public void setDateModified(Date m) {
    // TODO : indicate this change in the database
    this.dateModified = m;
  }

  /**
   * Getter for date created.
   * @return The date it was created.
   */
  public Date getDateCreated() {
    return this.created;
  }

  /**
   * Getter for the date modified last.
   * @return The date modified last.
   */
  public Date getDateModified() {
    return this.dateModified;
  }

  /**
   * Getter for the title.
   * @return The title.
   */
  public String getTitle() {
    return this.title;
  }

}
