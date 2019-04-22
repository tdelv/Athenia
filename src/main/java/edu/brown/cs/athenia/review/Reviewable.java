package edu.brown.cs.athenia.review;

/**
 * Reviewable is the interface that has a "
 * @author makaylamurphy
 *
 */
public interface Reviewable {

  public int getRating(); // gets rating

  // will be List<ArrayList<String>> for Conjugation, String for Note, String
  // for Tag, and String[] for Vocab.
  public Object getContent(); // generates reviewable content

}
