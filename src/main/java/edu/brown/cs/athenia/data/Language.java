package edu.brown.cs.athenia.data;

public class Language extends Modularized {
  private String name;
  // TODO: some sort of recent list storing the most recent
  // free notes according to some date value

  /**
   * Constructors
   */

  public Language(String name) {
    this.name = name;
  }

  /**
   * Getters
   */

  public String getName() {
    return this.name;
  }

}
