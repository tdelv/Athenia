package edu.brown.cs.athenia.main;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.athenia.data.modules.Module;

/**
 * Athenia is our object class that calls on everything. Has ReviewMode and
 * other major classes as objects.
 * @author makaylamurphy
 *
 */
public class Athenia {

  private List<Module> allModules;

  public Athenia() {
    allModules = new ArrayList<Module>();
  }

  public List<Module> getAllModules() {
    return allModules;
  }

}
