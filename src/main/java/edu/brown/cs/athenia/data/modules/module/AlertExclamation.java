package edu.brown.cs.athenia.data.modules.module;

import edu.brown.cs.athenia.data.FreeNote;
import edu.brown.cs.athenia.data.modules.Text;

/**
 * Represents an alert or exlamation - a reminder such as "Test on Thursday!".
 * @author makaylamurphy
 *
 */
public class AlertExclamation extends Text {

  public AlertExclamation(String a) {
    super(a);
  }

  public AlertExclamation(String a, FreeNote f) {
    super(a, f);
  }

  public StorageType getType(){
    return StorageType.ALERT_EXCLAMATION;
  }
}
