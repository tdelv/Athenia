package edu.brown.cs.athenia.data.modules;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table is an abstract class that represents any kind of table or mapping. All
 * tables are reviewable modules.
 * @author makaylamurphy
 *
 */
public abstract class Table extends Module {

  private Map<String, String> table;
  private List<String> termList;

  public Table() {
    this.table = new HashMap<String, String>();
    this.termList = new ArrayList<String>();
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
    this.termList.add(term);
    this.table.put(term, def);
  }

  public List<String> getTermList() {
    return termList;
  }

  // TODO : something to update the map?
  // like how could you update a key?

  /**
   * Removes a mapping.
   * @param term
   * @return
   */
  public String remove(String term) {
    this.setDateModified(new Date());
    termList.remove(term);
    return table.remove(term);
  }

  public Map<String, String> getTable() {
    return table;
  }

}
