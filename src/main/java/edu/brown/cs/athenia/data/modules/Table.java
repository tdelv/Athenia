package edu.brown.cs.athenia.data.modules;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Table is an abstract class that represents any kind of table or mapping. All
 * tables are reviewable modules.
 * @author makaylamurphy
 *
 */
public abstract class Table extends Module {

  private Map<String, String> table;

  public Table() {
    table = new HashMap<String, String>();
  }

  /**
   * Adds a mapping.
   * @param term
   *          The term to be defined (in conjugation, the pronoun).
   * @param def
   *          The definition (in conjugation, the verb).
   */
  public void add(String term, String def) {
    this.setDateModified(new Date());
    table.put(term, def);
  }

  /**
   * Removes a mapping.
   * @param term
   * @return
   */
  public String remove(String term) {
    this.setDateModified(new Date());
    return table.remove(term);
  }

  public Map<String, String> getTable() {
    return table;
  }

}
