package edu.brown.cs.athenia.data.modules.module;

/**
 * Tag is a module that is a small text associated with another modules. A
 * module can have multiple tags.
 * @author makaylamurphy
 *
 */
public class Tag {

  private String tag;

  // TODO : should tags have some sort of "datemodified" "datecreated" "how many modules"
  //          fields? this may make it easier to filter, sort, etc. on the front end -- jason

  public Tag(String t) {
    tag = t;
  }

  public String getContent() {
    return tag;
  }
}
