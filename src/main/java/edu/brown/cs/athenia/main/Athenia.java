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

  /**
   * The review method created a ReviewMode that takes in the info the user
   * wants to review. It then creates a ModuleRanker that ranks the modules
   * based on rating and date last reviewed. Then, updates all the used modules'
   * date last reviewed.
   * @param tagsToReview
   *          : the Tags user wants to review.
   * @param startDateCreated
   *          : the start date user wants to start reviewing at
   * @param endDateCreated
   *          : the end date user wants to start reviewing at.
   * @return : the ranked modules to review, with the first index being the
   *         hardest and last reviewed and the last being the easiest and
   *         closest reviewed.
   */
  public List<Reviewable> review(List<Tag> tagsToReview, Date startDateCreated,
      Date endDateCreated) {
    ReviewMode review = new ReviewMode(this, tagsToReview, startDateCreated,
        endDateCreated);
    // get modules to review with given tags and in dates
    List<Reviewable> modulesToReview = review.review();
    ModuleRanker ranker = new ModuleRanker();
    List<Reviewable> rankedModules = ranker.rank(modulesToReview);
    // updated date last reviewed
    for (Reviewable mod : rankedModules) {
      mod.setDateLastReviewed(new Date());
    }
    return rankedModules;

  }

  // TODO : public method for getting a list of languages for the user to
  // choose from (of the one's they have -- used for the language landing page)

  public List<String> getLanguages() {
    return new ArrayList<String>(languages.keySet());
  }

}
