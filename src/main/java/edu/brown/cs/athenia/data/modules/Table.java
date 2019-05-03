package edu.brown.cs.athenia.data.modules;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.brown.cs.athenia.data.FreeNote;

/**
 * Table is an abstract class that represents any kind of table or mapping. All
 * tables are reviewable modules.
 * @author makaylamurphy
 *
 */
public abstract class Table extends Module {

  private List<Pair> table;

  public Table() {
    super();
    this.table = new ArrayList<Pair>();
  }

  public Table(FreeNote f) {
    super(f);
    this.table = new ArrayList<Pair>();
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
    this.table.add(new Pair(term, def));
  }

  public void add(String term, String def, int index) {
    this.setDateModified(new Date());
    this.table.add(new Pair(term, def));
  }

  public void update(String updatedTerm, String updatedDef, int index) {
    this.setDateModified(new Date());
    table.get(index).updatePair(updatedTerm, updatedDef);
  }

  /**
   * Removes a mapping.
   * @return
   */
  public boolean remove(Pair p) {
    this.setDateModified(new Date());
    return this.table.remove(p);
  }

  public void remove(int index) {
    this.setDateModified(new Date());
    this.table.remove(index);
  }

  public List<Pair> getTable() {
    return table;
  }

}
