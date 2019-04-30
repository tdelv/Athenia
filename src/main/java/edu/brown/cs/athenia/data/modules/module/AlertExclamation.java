package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.modules.Module;

public class AlertExclamation extends Module {

  private String alert;

  public AlertExclamation(String a) {
    alert = a;
  }

  public void update(Object a) {
    alert = (String) a;
  }

  public String getContent() {
    return alert;
  }

}
