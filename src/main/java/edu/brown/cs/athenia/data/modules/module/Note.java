package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.FreeNote;
import edu.brown.cs.athenia.data.modules.Text;
import edu.brown.cs.athenia.review.Reviewable;

/**
 * Note represents a note module, which is Reviewable. A note is a free text
 * within freenotes.
 * @author makaylamurphy
 *
 */
public class Note extends Text implements Reviewable {

  // rating is originally set to 1 (medium).
  private int rating;

  public Note(String n) {
    super(n);
  }

  public Note(String n, FreeNote f) {
    super(n, f);
  }

  /**
   * Getter for the rating from 0 (easy) to 2 (hard).
   * @return Returns int rating.
   */
  public int getRating() {
    return this.rating;
  }

  /**
   * Setter for rating.
   */
  public void setRating(int r) {
    this.rating = r;
  }

  /**
   * Getter for type.
   */
  public StorageType getType() {
    return StorageType.NOTE;
  }
}
