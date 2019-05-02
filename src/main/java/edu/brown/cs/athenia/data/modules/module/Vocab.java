package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.review.Reviewable;

/**
 * Vocab is a module that has a term-definition form.
 * @author makaylamurphy
 *
 */
public class Vocab extends Module implements Reviewable {

  // rating is originally set to 1 (medium).
  private int rating;
  private String term;
  private String definition;

  public Vocab(String t, String d) {
    this.term = t;
    this.definition = d;
  }

  /**
   * Getter for the rating from 0 (easy) to 2 (hard).
   * @return Returns int rating.
   */
  public int getRating() {
    return this.rating;
  }

  public void setRating(int r) {
    this.rating = r;
  }

  public String getTerm() {
    return this.term;
  }

  public String getDefinition() {
    return this.definition;
  }

  public void updateTerm(String t) {
    this.term = t;
  }

  public void updateDefition(String d) {
    this.definition = d;
  }

  public void updateVocab(String t, String d) {
    this.term = t;
    this.definition = d;
  }

}
