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

  /**
   * Getter for the title.
   * @return The title.
   */
  public String getTitle() {
    return title;
  }

}
