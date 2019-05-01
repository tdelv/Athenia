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
  }

}
