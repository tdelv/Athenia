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

  @Override
  public boolean equals(Object o) {
    return tag.equals((String) o);
  }

  @Override
  public int hashCode() {
    return tag.hashCode();
  }
}
