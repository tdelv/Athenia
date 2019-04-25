package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.review.ReviewableModule;

/**
 * Vocab is a module that has a term-definition form.
 * @author makaylamurphy
 *
 */
public class Vocab extends ReviewableModule {

  private String[] vocab;

  public void review(String term, String definition) {
    vocab = new String[2];
    vocab[0] = term;
    vocab[1] = definition;
  }

  public String[] getContent() {
    return vocab;
  }

  public void update(Object voc) {
    vocab = (String[]) voc;
  }

}
