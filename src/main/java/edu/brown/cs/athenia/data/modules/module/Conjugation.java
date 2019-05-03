package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.modules.Table;
import edu.brown.cs.athenia.review.Reviewable;

/**
 * Conjugation represents our Conjugation module. A Conjugation is a table-like
 * module that essentially adds new mappings of conjugations through Table.
 * @author makaylamurphy
 *
 */
public class Conjugation extends Table implements Reviewable {

  // rating is originally set to 1 (medium).
  private int rating;

  private String header;

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

  public void setHeader(String head) {
    this.header = head;
  }

  public String getHeader() {
    return this.header;
  }

}
