package edu.brown.cs.athenia.data.modules;

/**
 * Tag is a small text associated with another modules. A module can have
 * multiple tags.
 * @author makaylamurphy
 *
 */
public class Tag {

  private String tag;

  public Tag(String t) {
    tag = t;
  }

  public String getTag() {
    return tag;
  }
}
