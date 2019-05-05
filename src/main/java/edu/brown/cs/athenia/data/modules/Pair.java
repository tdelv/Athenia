package edu.brown.cs.athenia.data.modules;

/**
 * Pair is a class that holds a Sting mapping - term to definition.
 * @author makaylamurphy
 *
 */
public class Pair {

  private String term;
  private String def;

  public Pair(String t, String d) {
    this.term = t;
    this.def = d;
  }

  /**
   * Getter for term.
   * @return : String term
   */
  public String getTerm() {
    return this.term;
  }

  /**
   * Getter for definition.
   * @return String definition.
   */
  public String getDefinition() {
    return this.def;
  }

  /**
   * Setter for term.
   * @param t
   *          : String term to change.
   */
  public void updateTerm(String t) {
    this.term = t;
  }

  /**
   * Setter for definition.
   * @param d
   *          : String definition to change.
   */
  public void updateDefinition(String d) {
    this.def = d;
  }

  /**
   * Setter for Pair. Updates both term and def.
   * @param t
   *          : String term to set.
   * @param d
   *          : String definition to set.
   */
  public void updatePair(String t, String d) {
    this.term = t;
    this.def = d;
  }

}
