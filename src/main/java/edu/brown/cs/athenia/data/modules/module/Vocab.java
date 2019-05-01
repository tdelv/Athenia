package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.modules.Table;

/**
 * Vocab is a module that has a term-definition form.
 * @author makaylamurphy
 *
 */
public class Vocab extends Table {

  public Vocab(String term, String definition) {
    super();
    this.add(term, definition);
  private String[] vocab;

  public void review(String term, String definition) {
    vocab = new String[2];
    vocab[0] = term;
    vocab[1] = definition;
  }

  // TODO : have some way to add the content to this? - this is a dummy thing
  //            for getting the connection between front and back end - from jason
  public void setContent(String term, String definition) {
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
