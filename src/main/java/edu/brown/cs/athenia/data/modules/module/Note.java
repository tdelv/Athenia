package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.review.Reviewable;

/**
 * Note represents a note module, which is Reviewable. A note is a free text
 * within freenotes.
 * @author makaylamurphy
 *
 */
public class Note extends Module implements Reviewable {

  public int getRating() {
    return 0;
  }

  public void review() {
  }
}
