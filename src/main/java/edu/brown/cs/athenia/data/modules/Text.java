package edu.brown.cs.athenia.data.modules;

import java.util.Date;

/**
 * A text object holds a String. It is any kind of text and is a
 * reviewablemodule.
 * @author makaylamurphy
 *
 */
public abstract class Text extends Module {

  private String text;

  public Text(String text) {
    this.text = text;
  }

  /**
   * Sets the new text.
   * @param newT
   */
  public void update(String newT) {
    this.setDateModified(new Date());
    this.text = newT;
  }

  /**
   * Gets the text.
   * @return
   */
  public String getText() {
    return text;
  }

}
