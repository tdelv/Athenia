package edu.brown.cs.athenia.data.modules;

import java.util.Date;

import edu.brown.cs.athenia.data.FreeNote;

/**
 * A text object holds a String. It is any kind of text and is a
 * reviewablemodule.
 * @author makaylamurphy
 *
 */
public abstract class Text extends Module {

  private String text;

  /**
   * Constructor for Text made in text's page.
   * @param text
   *          : the text to return.
   */
  public Text(String text) {
    // TODO: store in database and generate (and set) id
    super();
    this.text = text;
  }

  /**
   * Constructor for Text made in a FreeNote.
   * @param text
   *          : the String text.
   * @param f
   *          : the FreeNote the text is in.
   */
  public Text(String text, FreeNote f) {
    // TODO: store in database and generate (and set) id
    super(f);
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
