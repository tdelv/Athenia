package edu.brown.cs.athenia.main;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import edu.brown.cs.athenia.data.Language;

/**
 * Athenia is our object class that calls on everything. Has ReviewMode and
 * other major classes as objects.
 * @author makaylamurphy
 *
 */
public class Athenia {

  private Map<String, Language> languages;
  private Language currLang;

  public Athenia() {
    languages = new HashMap<String, Language>();
  }

  public void setCurrLang(String lang) {
    if (languages.containsKey(lang)) {
      currLang = languages.get(lang);
    } else {
      currLang = this.addLanguage(lang);
    }
  }

  public Language addLanguage(String lang) {
    Language language = new Language();
    languages.put(lang, language);
    return language;
  }

  public Language getCurrLanguage() {
    return currLang;
  }
  /*
   * public List<Object> review(Date startDateCreated, Date endDateCreated) {
   * ReviewMode review = new ReviewMode(this, currLang.getTagList(),
   * startDateCreated, endDateCreated);
   * 
   * }
   */

  // TODO : public method for getting a list of languages for the user to
  //          choose from (of the one's they have -- used for the language landing page)

  public List<String> getLanguages() {
    return new ArrayList<String>(languages.keySet());
  }

}
