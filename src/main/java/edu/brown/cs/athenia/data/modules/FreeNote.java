package edu.brown.cs.athenia.data.modules;

import java.util.ArrayList;
import java.util.List;

/**
 * FreeNote represents our FreeNodes, which have modules, tags, and a state of
 * data created, date modified, and title.
 * @author makaylamurphy
 *
 */
public class FreeNote {

  private List<Module> modules;
  private String title;

  // TODO: some sort of id variable -- may make it easier to query the info
  //      for the front end

  // TODO: some type of way to store tags and dates for these FreeNote
  //        > stores the date created, edited, accessed?

  public FreeNote(String t) {
    modules = new ArrayList<Module>();
    title = t;
  }

  /**
   * Getter for list of Modules.
   * @return list of modules
   */
  public List<Module> getModules() {
    return modules;
  }

  // TODO : method for getting a list of all tags in the sub modules
  //        > used to display on the freenotes landing and home pages

  // TODO : methods for getting and setting the date variables of the specific note

  /**
   * Getter for the title.
   * @return The title.
   */
  public String getTitle() {
    return title;
  }

}
