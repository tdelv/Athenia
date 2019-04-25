package edu.brown.cs.athenia.review;

import edu.brown.cs.athenia.data.modules.Module;

public abstract class ReviewableModule extends Module implements Reviewable {

  // rating is originally set to 1 (medium).
  private int rating;

  public ReviewableModule() {
    rating = 1;
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
