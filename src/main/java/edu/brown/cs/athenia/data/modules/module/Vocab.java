package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.FreeNote;
import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.data.modules.Pair;
import edu.brown.cs.athenia.review.Reviewable;

/**
 * Vocab is a module that has a term-definition form.
 * @author makaylamurphy
 *
 */
public class Vocab extends Module implements Reviewable {

  // rating is originally set to 1 (medium).
  private int rating;
  private Pair pair;

  public Vocab(String t, String d) {
    // TODO: store in database and generate (and set) id
    super();
    pair = new Pair(t, d);
  }

  public Vocab(String t, String d, FreeNote f) {
    // TODO: store in database and generate (and set) id
    super(f);
    pair = new Pair(t, d);
  }

  public Pair getPair() {
    return pair;
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
