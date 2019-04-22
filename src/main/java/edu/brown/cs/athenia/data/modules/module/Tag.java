package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.modules.Module;

/**
 * Tag is a module that is a small text associated with another modules. A
 * module can have multiple tags.
 * @author makaylamurphy
 *
 */
public class Tag extends Module {

  private String tag;

  public Tag(String t) {
    tag = t;
  }

  public String getContent() {
    return tag;
  }
}
