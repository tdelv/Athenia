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

  /**
   * Getter for String of tag.
   * @return : The String of Tag.
   */
  public String getTag() {
    return tag;
  }

  /**
   * Equals override for Tag - checks String.
   */
  @Override
  public boolean equals(Object o) {
    return tag.equals(((Tag) o).getTag());
  }

  /**
   * Override for HashCode - hashes on String tag.
   */
  @Override
  public int hashCode() {
    return tag.hashCode();
  }
}
