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
  private int height;

  public Table() {
    super();
    this.table = new ArrayList<Pair>();
    this.height = 3;
  }

  public Table(FreeNote f) {
    super(f);
    this.table = new ArrayList<Pair>();
    this.height = 3;
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

  /**
   * Adds a mapping (Paid) of term and def at a specific index in the table.
   * @param term
   * @param def
   * @param index
   */
  public void add(String term, String def, int index) {
    this.setDateModified(new Date());
    this.table.add(new Pair(term, def));
  }

  /**
   * Updates Pair at given term, definition, and index.
   * @param updatedTerm
   *          : the String term update.
   * @param updatedDef
   *          : the String definition update.
   * @param index
   *          : the int index location for updated Pair.
   */
  public void update(String updatedTerm, String updatedDef, int index) {
    this.setDateModified(new Date());
    table.get(index).updatePair(updatedTerm, updatedDef);
  }

  /**
   * Removes a mapping.
   * @return : the boolean of whether something is found/removed.
   */
  public boolean remove(Pair p) {
    this.setDateModified(new Date());
    return this.table.remove(p);
  }

  /**
   * Removes a Pair at an index.
   * @param index
   *          : the int index to remove.
   */
  public void remove(int index) {
    this.setDateModified(new Date());
    this.table.remove(index);
  }

  /**
   * Getter for full table.
   * @return : the List of Pairs (table).
   */
  public List<Pair> getTable() {
    return table;
  }

  /**
   * Getter for rows that are displayed on the front page.
   * @return : the amount of rows.
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Setter for number of rows that are displayed on the front page.
   * @param height
   *          : The amount of rows to display on the front page.
   */
  public void setHeight(int height) {
    this.height = height;
  }

}
