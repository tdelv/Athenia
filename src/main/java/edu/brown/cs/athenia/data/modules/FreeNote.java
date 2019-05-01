package edu.brown.cs.athenia.data.modules;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * FreeNote represents our FreeNodes, which have modules, tags, and a state of
 * data created, date modified, and title.
 * @author makaylamurphy
 *
 */
public class FreeNote {

  private List<Module> modules;
  private List<Tag> tags;
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
    this.tags = new ArrayList<Tag>();
    this.created = new Date();
    this.dateModified = this.created;
    this.title = t;
  }

  /**
   * Getter for list of Modules.
   * @return list of modules
   */
  public List<Module> getModules() {
    return this.modules;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public String getId() {
    return this.id;
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

  // TODO : updater method for a specific module in the freenote
  public void updateModule(Module module) {
    // update the appropriate module if it exists in the module list
  }

  // TODO : method for getting a list of all tags in the sub modules
  // > used to display on the freenotes landing and home pages

  // TODO : methods for getting and setting the date variables of the specific
  // note

  /**
   * Getter for the title.
   * @return The title.
   */
  public String getTitle() {
    return this.title;
  }

}
