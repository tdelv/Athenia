package edu.brown.cs.athenia.review;

import java.util.Date;

/**
 * Reviewable is the interface that has a "
 * @author makaylamurphy
 *
 */
public interface Reviewable {

  public void setRating(int r); // sets rating

  public int getRating(); // gets rating

  public Date getDateCreated(); // needs to have for reviewing

  public Date getDateLastReviewed(); // needs to have for reviewing

}
