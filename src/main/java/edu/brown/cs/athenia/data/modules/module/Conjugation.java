package edu.brown.cs.athenia.data.modules.module;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.athenia.review.ReviewableModule;

/**
 * Conjugation represents our Conjugation module. A Conjugation is a table-like
 * module.
 * @author makaylamurphy
 *
 */
public class Conjugation extends ReviewableModule {

  // represents a conjugation table of columns [subject and verb, maybe others]
  // and unknown amount of rows
  private List<ArrayList<String>> conjugationTable;

  public Conjugation() {
    conjugationTable = new ArrayList<ArrayList<String>>();
  }

  public List<ArrayList<String>> getContent() {
    return conjugationTable;
  }

  public void update(Object conjugations) {
    conjugationTable = (List<ArrayList<String>>) conjugations;
  }

}
