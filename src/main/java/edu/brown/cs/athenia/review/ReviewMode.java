package edu.brown.cs.athenia.review;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.data.modules.Tag;
import edu.brown.cs.athenia.main.Athenia;

/**
 * ReviewMode calls .review() for each of Athenia's modules and sets the ranks
 * by calling ModuleRanker. Uses an algorithm to generate a sorted list of
 * modules. Can filter the result by tag.
 * @author makaylamurphy
 *
 */
public class ReviewMode {

  private Language currLang;
  private List<Tag> tags;
  private Date startDateCreated;
  private Date endDateCreated;

  // TODO: update all dates last reviewed for the modules found
  public ReviewMode(Athenia project, List<Tag> tags, Date startDateCreated,
      Date endDateCreated) {
    // get the current language from Athenia
    this.currLang = project.getCurrLanguage();
    this.tags = tags;
    this.startDateCreated = startDateCreated;
    this.endDateCreated = endDateCreated;
  }

  /**
   * ReviewMode's review finds the Reviewable modules we want to review based on
   * the tags, and creation date range we want to review.
   * @return : Returns an unranked (unsorted) List of Reviewable modules.
   */
  public List<Reviewable> review() {

    List<Reviewable> modulesToReview = new ArrayList<Reviewable>();

    for (Tag tag : tags) {
      List<Module> modulesFromTag = currLang.getModuleListFromTag(tag);
      for (Module mod : modulesFromTag) {
        // is module to review if is reviewable and is either equal to start or
        // end date created or between those two dates
        if (mod instanceof Reviewable
            && ((mod.getDateCreated().equals(startDateCreated)
                || mod.getDateCreated().equals(endDateCreated))
                || (mod.getDateCreated().after(startDateCreated)
                    && mod.getDateCreated().before(endDateCreated)))) {
          modulesToReview.add((Reviewable) mod);
        }
      }
    }

    return modulesToReview;
  }

}
