package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.review.Reviewable;

/**
 * Vocab is a module that has a term-definition form.
 * @author makaylamurphy
 *
 */
public class Vocab extends Module implements Reviewable {

  private String[] vocab;

  public void review(String term, String definition) {
    vocab = new String[2];
    vocab[0] = term;
    vocab[1] = definition;
  }

  public String[] getContent() {
    return vocab;
  }

}
