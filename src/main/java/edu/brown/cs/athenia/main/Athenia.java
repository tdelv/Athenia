package edu.brown.cs.athenia.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.data.modules.Tag;
import edu.brown.cs.athenia.review.ModuleRanker;
import edu.brown.cs.athenia.review.ReviewMode;
import edu.brown.cs.athenia.review.Reviewable;

/**
 * Athenia is our object class that calls on everything. Has ReviewMode and
 * other major classes as objects.
 * @author makaylamurphy
 *
 */
public class Athenia {

  private String userId;
  private Map<String, Language> languages;
  private Language currLang;

  public Athenia(String userId) {
    this.userId = userId;
    this.languages = new HashMap<String, Language>();
  }

  public void setCurrLang(String lang) {
    if (languages.containsKey(lang)) {
      currLang = languages.get(lang);
    } else {
      currLang = this.addLanguage(lang);
    }
  }

  public Language addLanguage(String lang) {
    Language language = new Language(lang);
    languages.put(lang, language);
    return language;
  }

  public void removeLanguage(String lang) {
    languages.remove(lang);
    // TODO from jason : remove this information from database too
  }

  public Language getCurrLanguage() {
    return currLang;
  }

  public List<Reviewable> review(List<Tag> tagsToReview, Date startDateCreated,
      Date endDateCreated) {
    ReviewMode review = new ReviewMode(this, tagsToReview, startDateCreated,
        endDateCreated);
    List<Reviewable> modulesToReview = review.review();
    ModuleRanker ranker = new ModuleRanker();
    List<Reviewable> rankedModules = ranker.rank(modulesToReview);
    return rankedModules;

  }

  // TODO : public method for getting a list of languages for the user to
  // choose from (of the one's they have -- used for the language landing page)

  public List<String> getLanguages() {
    return new ArrayList<String>(languages.keySet());
  }

}
