package edu.brown.cs.athenia.data;

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

  // TODO: some sort of id variable -- may make it easier to query the info
  // for the front end DONE

  // TODO: some type of way to store tags and dates for these FreeNote
  // > stores the date created, edited, accessed for this specifically DONE

  public FreeNote(String t, String id) {
    this.id = id;
    this.modules = new ArrayList<Module>();
    this.tags = new HashMap<String, Tag>();
    this.created = new Date();
    this.dateModified = this.created;
    this.title = t;
  }

  public String getId() {
    return this.id;
  }

  public List<Module> getModules() {
    return this.modules;
  }

  public boolean removeModule(Module m) {
    this.dateModified = new Date();
    m.removeFreeNote();
    return this.modules.remove(m);
  }

  public void addModule(Module m) {
    this.dateModified = new Date();
    this.modules.add(m);
  }

  public Collection<Tag> getTags() {
    return tags.values();
  }

  public void addTag(Tag t) {
    this.dateModified = new Date();
    tags.put(t.getTag(), t);
  }

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
