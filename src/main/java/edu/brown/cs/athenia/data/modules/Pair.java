package edu.brown.cs.athenia.data.modules;

public class Pair {

  private String term;
  private String def;

  public Pair(String t, String d) {
    this.term = t;
    this.def = d;
  }

  public String getTerm() {
    return this.term;
  }

  public String getDefinition() {
    return this.def;
  }

  public void updateTerm(String t) {
    this.term = t;
  }

  public void updateDefinition(String d) {
    this.def = d;
  }

  public void updatePair(String t, String d) {
    this.term = t;
    this.def = d;
  }

}
