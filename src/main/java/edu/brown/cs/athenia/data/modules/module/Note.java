package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.review.ReviewableModule;

/**
 * Note represents a note module, which is Reviewable. A note is a free text
 * within freenotes.
 * @author makaylamurphy
 *
 */
public class Note extends ReviewableModule {

  private String note;

  public Note(String n) {
    note = n;
  }

  public void update(Object newNote) {
    note = (String) newNote;
  }

}
