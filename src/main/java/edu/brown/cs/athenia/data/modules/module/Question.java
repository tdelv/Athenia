package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.FreeNote;
import edu.brown.cs.athenia.data.modules.Text;

/**
 * A question such as "Do I need to study this?".
 * @author makaylamurphy
 *
 */
public class Question extends Text {

  public Question(String q) {
    super(q);
  }

  public Question(String q, FreeNote f) {
    super(q, f);
  }

}
