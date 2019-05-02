package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.modules.Table;
import edu.brown.cs.athenia.review.Reviewable;

/**
 * Vocab is a module that has a term-definition form.
 * @author makaylamurphy
 *
 */
public class Vocab extends Table implements Reviewable {

  // rating is originally set to 1 (medium).
  private int rating;

  public Vocab(String term, String definition) {
    super();
    this.add(term, definition);
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

}
