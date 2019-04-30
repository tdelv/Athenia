package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.modules.Module;

public class Question extends Module {

  private String question;

  public Question(String q) {
    question = q;
  }

  public void update(Object q) {
    question = (String) q;
  }

  public String getContent() {
    return question;
  }

}
